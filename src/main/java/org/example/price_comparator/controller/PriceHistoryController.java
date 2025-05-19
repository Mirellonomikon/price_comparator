package org.example.price_comparator.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.ProductPriceHistoryDto;
import org.example.price_comparator.service.PriceHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/price-history")
@Slf4j
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    public PriceHistoryController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductPriceHistoryDto> getProductPriceHistory(
            @PathVariable String productId,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting price history for product ID: {}, store: {}, from: {}, to: {}",
                productId, storeName, startDate, endDate);

        ProductPriceHistoryDto priceHistory = priceHistoryService.getProductPriceHistory(productId, storeName, startDate, endDate);

        return ResponseEntity.ok(priceHistory);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductPriceHistoryDto>> getPriceHistoryByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting price history for category: {}, store: {}, from: {}, to: {}",
                category, storeName, startDate, endDate);

        List<ProductPriceHistoryDto> priceHistory = priceHistoryService.getPriceHistoryByCategory(category, storeName, startDate, endDate);

        return ResponseEntity.ok(priceHistory);
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<ProductPriceHistoryDto>> getPriceHistoryByBrand(
            @PathVariable String brand,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting price history for brand: {}, store: {}, from: {}, to: {}",
                brand, storeName, startDate, endDate);

        List<ProductPriceHistoryDto> priceHistory = priceHistoryService.getPriceHistoryByBrand(brand, storeName, startDate, endDate);

        return ResponseEntity.ok(priceHistory);
    }
}

