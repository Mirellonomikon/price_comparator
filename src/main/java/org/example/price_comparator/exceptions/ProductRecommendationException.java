package org.example.price_comparator.exceptions;

public class ProductRecommendationException extends RuntimeException {

    public ProductRecommendationException(String message) {
        super(message);
    }

    public ProductRecommendationException(String message, Throwable cause) {
        super(message, cause);
    }
}
