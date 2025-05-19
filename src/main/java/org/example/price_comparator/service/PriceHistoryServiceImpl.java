package org.example.price_comparator.service;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.PriceHistoryPointDto;
import org.example.price_comparator.dto.ProductPriceHistoryDto;
import org.example.price_comparator.exceptions.PriceHistoryException;
import org.example.price_comparator.model.Discount;
import org.example.price_comparator.model.Price;
import org.example.price_comparator.model.Product;
import org.example.price_comparator.model.Store;
import org.example.price_comparator.repository.DiscountRepository;
import org.example.price_comparator.repository.PriceRepository;
import org.example.price_comparator.repository.ProductRepository;
import org.example.price_comparator.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final PriceRepository priceRepository;
    private final DiscountRepository discountRepository;

    public PriceHistoryServiceImpl(ProductRepository productRepository,
                                   StoreRepository storeRepository,
                                   PriceRepository priceRepository,
                                   DiscountRepository discountRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public ProductPriceHistoryDto getProductPriceHistory(String productId, String storeName,
                                                         LocalDate startDate, LocalDate endDate) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new PriceHistoryException("Product not found with ID: " + productId));

            // Find the store if specified
            Store store = null;
            if (storeName != null && !storeName.isEmpty()) {
                store = storeRepository.findByName(storeName)
                        .orElseThrow(() -> new PriceHistoryException("Store not found with name: " + storeName));
            }

            List<PriceHistoryPointDto> priceHistory = getPriceHistoryPoints(product, store, startDate, endDate);

            // Create and return the DTO
            return createProductPriceHistoryDto(product, storeName, priceHistory);
        } catch (PriceHistoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting price history for product ID: {}", productId, e);
            throw new PriceHistoryException("Error getting price history: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductPriceHistoryDto> getPriceHistoryByCategory(String category, String storeName,
                                                                  LocalDate startDate, LocalDate endDate) {
        try {
            // Find products in the category
            List<Product> products = productRepository.findByProductCategoryIgnoreCase(category);
            if (products.isEmpty()) {
                log.info("No products found in category: {}", category);
                return Collections.emptyList();
            }
            // Find the store if specified
            Store store = null;
            if (storeName != null && !storeName.isEmpty()) {
                store = storeRepository.findByName(storeName)
                        .orElseThrow(() -> new PriceHistoryException("Store not found with name: " + storeName));
            }

            return getProductsPriceHistory(products, store, storeName, startDate, endDate);
        } catch (PriceHistoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting price history for category: {}", category, e);
            throw new PriceHistoryException("Error getting price history by category: " + e.getMessage(), e);
        }
    }
    @Override
    public List<ProductPriceHistoryDto> getPriceHistoryByBrand(String brand, String storeName,
                                                               LocalDate startDate, LocalDate endDate) {
        try {
            // Find products of the brand
            List<Product> products = productRepository.findByBrandIgnoreCase(brand);
            if (products.isEmpty()) {
                log.info("No products found for brand: {}", brand);
                return Collections.emptyList();
            }
            // Find the store if specified
            Store store = null;
            if (storeName != null && !storeName.isEmpty()) {
                store = storeRepository.findByName(storeName)
                        .orElseThrow(() -> new PriceHistoryException("Store not found with name: " + storeName));
            }

            return getProductsPriceHistory(products, store, storeName, startDate, endDate);
        } catch (PriceHistoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting price history for brand: {}", brand, e);
            throw new PriceHistoryException("Error getting price history by brand: " + e.getMessage(), e);
        }
    }

    private List<ProductPriceHistoryDto> getProductsPriceHistory(List<Product> products, Store store,
                                                                 String storeName, LocalDate startDate, LocalDate endDate) {
        List<ProductPriceHistoryDto> result = new ArrayList<>();

        for (Product product : products) {
            // Get price history points
            List<PriceHistoryPointDto> priceHistory = getPriceHistoryPoints(product, store, startDate, endDate);

            if (priceHistory.isEmpty()) {
                continue;
            }

            ProductPriceHistoryDto dto = createProductPriceHistoryDto(product, storeName, priceHistory);
            result.add(dto);
        }

        return result;
    }

    private List<PriceHistoryPointDto> getPriceHistoryPoints(Product product, Store store,
                                                             LocalDate startDate, LocalDate endDate) {
        // If dates are not specified, use defaults
        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.now().minusMonths(3);
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();
        // Get prices for the product
        List<Price> prices;
        if (store != null) {
            // Filter by store if specified
            prices = priceRepository.findByProductAndStoreAndDateBetweenOrderByDateAsc(
                    product, store, effectiveStartDate, effectiveEndDate);
        } else {
            // Get all prices across stores
            prices = priceRepository.findByProductAndDateBetweenOrderByDateAsc(
                    product, effectiveStartDate, effectiveEndDate);
        }
        if (prices.isEmpty()) {
            return Collections.emptyList();
        }
        // Map of date to price point to handle multiple prices on the same day
        Map<LocalDate, Map<Store, PriceHistoryPointDto>> pricePointsByDate = new HashMap<>();
        // Process each price
        for (Price price : prices) {
            LocalDate date = price.getDate();
            Store priceStore = price.getStore();
            // Get or create the map for this date
            if (!pricePointsByDate.containsKey(date)) {
                pricePointsByDate.put(date, new HashMap<>());
            }

            // Skip if we already have a price point for this store on this date
            if (pricePointsByDate.get(date).containsKey(priceStore)) {
                continue;
            }
            // Create a price history point
            PriceHistoryPointDto pricePoint = new PriceHistoryPointDto();
            pricePoint.setDate(date);
            pricePoint.setPrice(price.getPrice());
            pricePoint.setCurrency(price.getCurrency());
            // Check if there is a discount active on this date
            Optional<Discount> discount = discountRepository
                    .findByProductAndStoreAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            product, priceStore, date, date);
            if (discount.isPresent()) {
                pricePoint.setIsDiscounted(true);
                pricePoint.setDiscountPercentage(discount.get().getPercentageOfDiscount());
                pricePoint.setOriginalPrice(price.getPrice());
                double discountedPrice = price.getPrice() * (1 - discount.get().getPercentageOfDiscount() / 100);
                pricePoint.setPrice(discountedPrice);
            } else {
                pricePoint.setIsDiscounted(false);
            }
            pricePointsByDate.get(date).put(priceStore, pricePoint);
        }
        // Flatten the map to a list of price points
        List<PriceHistoryPointDto> result = new ArrayList<>();
        if (store != null) {
            // If a specific store is requested, return a single series
            for (Map.Entry<LocalDate, Map<Store, PriceHistoryPointDto>> entry : pricePointsByDate.entrySet()) {
                if (entry.getValue().containsKey(store)) {
                    result.add(entry.getValue().get(store));
                }
            }
        } else {
            // If no specific store is requested, return the lowest price for each date
            for (Map.Entry<LocalDate, Map<Store, PriceHistoryPointDto>> entry : pricePointsByDate.entrySet()) {
                entry.getValue().values().stream()
                        .min(Comparator.comparing(PriceHistoryPointDto::getPrice)).ifPresent(result::add);
            }
        }
        // Sort by date
        result.sort(Comparator.comparing(PriceHistoryPointDto::getDate));
        return result;
    }

    private ProductPriceHistoryDto createProductPriceHistoryDto(Product product, String storeName,
                                                                List<PriceHistoryPointDto> priceHistory) {
        ProductPriceHistoryDto dto = new ProductPriceHistoryDto();
        dto.setProductId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setProductCategory(product.getProductCategory());
        dto.setBrand(product.getBrand());
        dto.setPackageQuantity(product.getPackageQuantity());
        dto.setPackageUnit(product.getPackageUnit());
        dto.setStoreName(storeName);
        dto.setPriceHistory(priceHistory);
        return dto;
    }
}

