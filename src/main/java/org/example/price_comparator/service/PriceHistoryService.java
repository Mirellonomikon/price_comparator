package org.example.price_comparator.service;

import org.example.price_comparator.dto.ProductPriceHistoryDto;

import java.time.LocalDate;
import java.util.List;

public interface PriceHistoryService {

    ProductPriceHistoryDto getProductPriceHistory(String productId, String storeName, LocalDate startDate, LocalDate endDate);
    List<ProductPriceHistoryDto> getPriceHistoryByCategory(String category, String storeName, LocalDate startDate, LocalDate endDate);
    List<ProductPriceHistoryDto> getPriceHistoryByBrand(String brand, String storeName, LocalDate startDate, LocalDate endDate);
}

