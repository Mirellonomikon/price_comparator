package org.example.price_comparator.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.ProductRecommendationDto;
import org.example.price_comparator.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/substitutes")
    public ResponseEntity<List<ProductRecommendationDto>> findSubstitutes(@RequestParam String productId,
            @RequestParam(defaultValue = "3") int limit) {
        log.info("Finding substitutes for product ID: {} with limit: {}", productId, limit);
        List<ProductRecommendationDto> substitutes = recommendationService.findSubstitutes(productId, limit);
        return ResponseEntity.ok(substitutes);
    }
    @GetMapping("/best-value")
    public ResponseEntity<List<ProductRecommendationDto>> findBestValueProducts(
            @RequestParam String category) {
        log.info("Finding best value products in category: {}", category);
        List<ProductRecommendationDto> bestValueProducts = recommendationService.findBestValueProducts(category);
        return ResponseEntity.ok(bestValueProducts);
    }
}


