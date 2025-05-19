package org.example.price_comparator.exceptions;

public class PriceHistoryException extends RuntimeException {

    public PriceHistoryException(String message) {
        super(message);
    }
    public PriceHistoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

