package org.example.price_comparator.exceptions;

public class PriceAlertException extends RuntimeException {

    public PriceAlertException(String message) {
        super(message);
    }

    public PriceAlertException(String message, Throwable cause) {
        super(message, cause);
    }
}

