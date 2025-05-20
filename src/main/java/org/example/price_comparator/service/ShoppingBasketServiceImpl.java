package org.example.price_comparator.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.basket.*;
import org.example.price_comparator.exceptions.BasketOptimizationException;
import org.example.price_comparator.model.Discount;
import org.example.price_comparator.model.Price;
import org.example.price_comparator.model.Product;
import org.example.price_comparator.model.Store;
import org.example.price_comparator.repository.DiscountRepository;
import org.example.price_comparator.repository.PriceRepository;
import org.example.price_comparator.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class ShoppingBasketServiceImpl implements ShoppingBasketService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final DiscountRepository discountRepository;

    public ShoppingBasketServiceImpl(ProductRepository productRepository,
                                     PriceRepository priceRepository,
                                     DiscountRepository discountRepository) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public OptimizedShoppingPlanDto optimizeBasket(ShoppingBasketDto basket, LocalDate date) {
        try {
            if (basket == null || basket.getItems() == null || basket.getItems().isEmpty()) {
                throw new BasketOptimizationException("Shopping basket cannot be empty");
            }

            // Maps to store the best prices and store information
            Map<String, StoreProductPrice> bestPrices = new HashMap<>();
            Map<String, StoreProductPrice> worstPrices = new HashMap<>();
            Map<String, List<OptimizedItemDto>> storeItems = new HashMap<>();

            // Process each basket item
            for (BasketItemDto item : basket.getItems()) {
                String productId = item.getProductId();
                Integer quantity = item.getQuantity();

                if (quantity <= 0) {
                    log.warn("Skipping item with invalid quantity: {}", productId);
                    continue;
                }

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new BasketOptimizationException("Product not found: " + productId));


                List<StoreProductPrice> storePrices = findAllStorePrices(product, date);

                if (storePrices.isEmpty()) {
                    log.warn("No price found for product: {}", productId);
                    continue;
                }

                // Find best and worst prices
                StoreProductPrice bestPrice = findExtremePrice(storePrices, true); // true for lowest price
                StoreProductPrice worstPrice = findExtremePrice(storePrices, false); // false for highest price

                // Store the best and worst prices
                bestPrices.put(productId, bestPrice);
                worstPrices.put(productId, worstPrice);

                // Create optimized item
                OptimizedItemDto optimizedItem = createOptimizedItem(product, bestPrice, quantity);

                // Add to store items map
                String storeName = bestPrice.getStore().getName();
                if (!storeItems.containsKey(storeName)) {
                    storeItems.put(storeName, new ArrayList<>());
                }
                storeItems.get(storeName).add(optimizedItem);
            }

            // Create store shopping lists
            List<StoreShoppingListDto> storeLists = new ArrayList<>();
            double totalCost = 0;
            double worstCaseCost = 0;
            String currency = null;

            for (Map.Entry<String, List<OptimizedItemDto>> entry : storeItems.entrySet()) {
                String storeName = entry.getKey();
                List<OptimizedItemDto> items = entry.getValue();

                // Calculate subtotal
                double subtotal = items.stream()
                        .mapToDouble(OptimizedItemDto::getTotalPrice)
                        .sum();

                // Round the subtotal
                subtotal = roundToTwoDecimals(subtotal);

                // Set currency from first item (assuming all items have same currency)
                if (currency == null && !items.isEmpty()) {
                    currency = items.getFirst().getCurrency();
                }

                // Create store shopping list
                StoreShoppingListDto storeList = new StoreShoppingListDto();
                storeList.setStoreName(storeName);
                storeList.setItems(items);
                storeList.setSubtotal(subtotal);
                storeList.setCurrency(currency);
                storeLists.add(storeList);

                totalCost += subtotal;
            }

            // Calculate worst case cost
            for (BasketItemDto item : basket.getItems()) {
                String productId = item.getProductId();
                Integer quantity = item.getQuantity();

                if (worstPrices.containsKey(productId)) {
                    StoreProductPrice worstPrice = worstPrices.get(productId);
                    worstCaseCost += worstPrice.getFinalPrice() * quantity;
                }
            }

            // Round the total values
            totalCost = roundToTwoDecimals(totalCost);
            worstCaseCost = roundToTwoDecimals(worstCaseCost);
            double totalSavings = roundToTwoDecimals(worstCaseCost - totalCost);

            // Create optimized shopping plan
            OptimizedShoppingPlanDto plan = new OptimizedShoppingPlanDto();
            plan.setStoreLists(storeLists);
            plan.setTotalCost(totalCost);
            plan.setWorstCaseCost(worstCaseCost);
            plan.setTotalSavings(totalSavings);
            plan.setCurrency(currency);
            plan.setTotalStores(storeLists.size());
            plan.setTotalItems(basket.getItems().size());

            return plan;

        } catch (BasketOptimizationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error optimizing shopping basket", e);
            throw new BasketOptimizationException("Error optimizing shopping basket: " + e.getMessage(), e);
        }
    }

    private List<StoreProductPrice> findAllStorePrices(Product product, LocalDate date) {
        // Find all prices for this product
        List<Price> prices = priceRepository.findByProductAndDateLessThanEqualOrderByDateDesc(product, date);

        if (prices.isEmpty()) {
            return Collections.emptyList();
        }

        // Group by store to get the most recent price for each store
        Map<Store, Price> latestPriceByStore = new HashMap<>();
        for (Price price : prices) {
            Store store = price.getStore();
            if (!latestPriceByStore.containsKey(store) ||
                    price.getDate().isAfter(latestPriceByStore.get(store).getDate())) {
                latestPriceByStore.put(store, price);
            }
        }

        // Create StoreProductPrice objects for each store
        List<StoreProductPrice> storePrices = new ArrayList<>();

        for (Map.Entry<Store, Price> entry : latestPriceByStore.entrySet()) {
            Store store = entry.getKey();
            Price price = entry.getValue();
            double originalPrice = price.getPrice();
            double finalPrice = originalPrice;
            Double discountPercentage = null;

            // Check for active discounts
            Optional<Discount> activeDiscount = discountRepository
                    .findByProductAndStoreAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            product, store, date, date);

            if (activeDiscount.isPresent()) {
                discountPercentage = activeDiscount.get().getPercentageOfDiscount();
                finalPrice = originalPrice * (1 - discountPercentage / 100);
            }

            storePrices.add(new StoreProductPrice(store, price, finalPrice, discountPercentage));
        }

        return storePrices;
    }

    private StoreProductPrice findExtremePrice(List<StoreProductPrice> storePrices, boolean findLowest) {
        if (storePrices.isEmpty()) {
            return null;
        }

        return storePrices.stream()
                .min((sp1, sp2) -> {
                    int comparison = Double.compare(sp1.getFinalPrice(), sp2.getFinalPrice());
                    return findLowest ? comparison : -comparison; // Invert for highest price
                })
                .orElse(null);
    }

    private OptimizedItemDto createOptimizedItem(Product product, StoreProductPrice storePrice, Integer quantity) {
        Price price = storePrice.getPrice();
        double unitPrice = roundToTwoDecimals(storePrice.getFinalPrice());
        double totalPrice = roundToTwoDecimals(unitPrice * quantity);

        double savingsPerUnit = 0;
        if (storePrice.getDiscountPercentage() != null) {
            savingsPerUnit = roundToTwoDecimals(price.getPrice() - unitPrice);
        }

        OptimizedItemDto item = new OptimizedItemDto();
        item.setProductId(product.getId());
        item.setProductName(product.getProductName());
        item.setBrand(product.getBrand());
        item.setPackageQuantity(product.getPackageQuantity());
        item.setPackageUnit(product.getPackageUnit());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(totalPrice);
        item.setCurrency(price.getCurrency());
        item.setOnDiscount(storePrice.getDiscountPercentage() != null);

        if (storePrice.getDiscountPercentage() != null) {
            item.setDiscountPercentage(roundToTwoDecimals(storePrice.getDiscountPercentage()));
        } else {
            item.setDiscountPercentage(null);
        }

        item.setSavingsPerUnit(savingsPerUnit);
        return item;
    }

    // Helper class to store price information (and avoid errors when calculating savings)
    @Data
    @AllArgsConstructor
    private static class StoreProductPrice {
        private Store store;
        private Price price;
        private Double finalPrice;
        private Double discountPercentage;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}






