package org.example.price_comparator.service;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.ProductRecommendationDto;
import org.example.price_comparator.exceptions.ProductRecommendationException;
import org.example.price_comparator.model.Discount;
import org.example.price_comparator.model.Price;
import org.example.price_comparator.model.Product;
import org.example.price_comparator.model.Store;
import org.example.price_comparator.repository.DiscountRepository;
import org.example.price_comparator.repository.PriceRepository;
import org.example.price_comparator.repository.ProductRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final DiscountRepository discountRepository;

    public RecommendationServiceImpl(ProductRepository productRepository, PriceRepository priceRepository, DiscountRepository discountRepository) {
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public List<ProductRecommendationDto> findSubstitutes(String productId, int limit) {
        try {
            // Find the original product by ID
            Optional<Product> originalProductOpt = productRepository.findById(productId);
            if (originalProductOpt.isEmpty()) {
                log.info("No product found with ID: {}", productId);
                return Collections.emptyList();
            }
            Product originalProduct = originalProductOpt.get();
            String originalName = originalProduct.getProductName();
            String originalCategory = originalProduct.getProductCategory();
            // Break the product name into words for partial matching
            String[] nameWords = originalName.split("\\s+");
            List<Product> similarProducts = new ArrayList<>();
            // Find products in the same category with similar names
            for (String word : nameWords) {
                if (word.length() >= 3) { // Only use words with at least 3 characters
                    List<Product> matches = productRepository
                            .findByProductCategoryAndProductNameContainingIgnoreCase(originalCategory, word);
                    similarProducts.addAll(matches);
                }
            }
            // Remove duplicates and the original product
            similarProducts = similarProducts.stream()
                    .filter(p -> !p.getId().equals(productId))
                    .distinct()
                    .collect(Collectors.toList());
            if (similarProducts.isEmpty()) {
                log.info("No similar products found for product ID: {}", productId);
                return Collections.emptyList();
            }

            LocalDate today = LocalDate.now();
            List<ProductRecommendationDto> recommendations = getProductRecommendations(similarProducts, today);

            // Apply the limit if it's greater than 0
            if (limit > 0) {
                return recommendations.stream()
                        .limit(limit)
                        .collect(Collectors.toList());
            }

            return recommendations;
        } catch (Exception e) {
            log.error("Error finding substitutes for product ID: {}", productId, e);
            throw new ProductRecommendationException("Error finding product substitutes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductRecommendationDto> findBestValueProducts(String category) {
        try {
            // Find all products in the specified category
            List<Product> categoryProducts = productRepository.findByProductCategoryIgnoreCase(category);
            if (categoryProducts.isEmpty()) {
                log.info("No products found in category: {}", category);
                return Collections.emptyList();
            }

            LocalDate today = LocalDate.now();
            List<ProductRecommendationDto> allRecommendations = getProductRecommendations(categoryProducts, today);

            // Group by product ID and find the best price for each product
            Map<String, List<ProductRecommendationDto>> productGroups = allRecommendations.stream()
                    .collect(Collectors.groupingBy(ProductRecommendationDto::getProductId));
            List<ProductRecommendationDto> bestValueRecommendations = new ArrayList<>();

            for (List<ProductRecommendationDto> productGroup : productGroups.values()) {
                // Sort by value per unit
                productGroup.sort(Comparator.comparing(ProductRecommendationDto::getValuePerUnit));
                // Get the best value
                double bestValue = productGroup.getFirst().getValuePerUnit();
                // Add all stores that offer this product at the best value
                for (ProductRecommendationDto recommendation : productGroup) {
                    if (Math.abs(recommendation.getValuePerUnit() - bestValue) < 0.001) {  // We use floating point
                        bestValueRecommendations.add(recommendation);
                    } else {
                        // Once we find a higher price, we can break since the list is sorted
                        break;
                    }
                }
            }
            // Sort the final list by value per unit (lowest first)
            return bestValueRecommendations.stream()
                    .sorted(Comparator.comparing(ProductRecommendationDto::getValuePerUnit))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding best value products for category: {}", category, e);
            throw new ProductRecommendationException("Error finding best value products: " + e.getMessage(), e);
        }
    }

    private List<ProductRecommendationDto> getProductRecommendations(List<Product> products, LocalDate date) {
        List<ProductRecommendationDto> recommendations = new ArrayList<>();
        for (Product product : products) {
            // Get all prices for this product across all stores
            List<Price> prices = priceRepository.findByProductAndDateLessThanEqualOrderByDateDesc(product, date);
            // Get the most recent price for each store
            Map<Store, Price> latestPriceByStore = new HashMap<>();
            for (Price price : prices) {
                Store store = price.getStore();
                if (!latestPriceByStore.containsKey(store) ||
                        price.getDate().isAfter(latestPriceByStore.get(store).getDate())) {
                    latestPriceByStore.put(store, price);
                }
            }
            // Process each store's price
            for (Map.Entry<Store, Price> entry : latestPriceByStore.entrySet()) {
                Store store = entry.getKey();
                Price price = entry.getValue();
                // Calculate value per unit
                Pair<Double, String> valuePerUnit = calculateValuePerUnit(product, price.getPrice());
                ProductRecommendationDto dto = new ProductRecommendationDto();
                dto.setProductId(product.getId());
                dto.setProductName(product.getProductName());
                dto.setProductCategory(product.getProductCategory());
                dto.setBrand(product.getBrand());
                dto.setPackageQuantity(product.getPackageQuantity());
                dto.setPackageUnit(product.getPackageUnit());
                dto.setStoreName(store.getName());
                dto.setPrice(price.getPrice());
                dto.setCurrency(price.getCurrency());
                dto.setValuePerUnit(valuePerUnit.getFirst());
                dto.setUnitType(valuePerUnit.getSecond());
                dto.setPriceDate(price.getDate());
                // Check for active discounts
                Optional<Discount> activeDiscount = discountRepository
                        .findByProductAndStoreAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                product, store, date, date);
                // Set discount information if available
                if (activeDiscount.isPresent()) {
                    Discount discount = activeDiscount.get();
                    dto.setOnDiscount(true);
                    dto.setPercentageOfDiscount(discount.getPercentageOfDiscount());
                    dto.setOriginalPrice(price.getPrice());
                    dto.setDiscountedPrice(price.getPrice() * (1 - discount.getPercentageOfDiscount() / 100));
                } else {
                    dto.setOnDiscount(false);
                }
                recommendations.add(dto);
            }
        }
        // Sort by value per unit
        return recommendations.stream()
                .sorted(Comparator.comparing(ProductRecommendationDto::getValuePerUnit))
                .collect(Collectors.toList());
    }

    private Pair<Double, String> calculateValuePerUnit(Product product, Double price) {
        String unit = product.getPackageUnit().toLowerCase();
        Double quantity = product.getPackageQuantity();
        // Standardize units for comparison
        if (unit.contains("g") && !unit.equals("kg")) {
            return Pair.of(price / (quantity / 1000), "kg");
        } else if (unit.contains("ml") || unit.contains("cl")) {
            double liters = unit.contains("ml") ? quantity / 1000 : quantity / 100;
            return Pair.of(price / liters, "l");
        } else {
            // Keep original unit
            return Pair.of(price / quantity, unit);
        }
    }
}



