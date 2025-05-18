package org.example.price_comparator.exceptions;

public class DiscountProcessingException extends RuntimeException {

    public DiscountProcessingException(String message) {
        super(message);
    }
    public DiscountProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

