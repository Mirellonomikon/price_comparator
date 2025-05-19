package org.example.price_comparator.service;

import org.example.price_comparator.dto.ProductRecommendationDto;
import java.util.List;

public interface RecommendationService {

    List<ProductRecommendationDto> findSubstitutes(String productId, int limit);
    List<ProductRecommendationDto> findBestValueProducts(String category);
}


