package org.example.price_comparator.exceptions;

public class BasketOptimizationException extends RuntimeException {

  public BasketOptimizationException(String message) {
    super(message);
  }
  public BasketOptimizationException(String message, Throwable cause) {
    super(message, cause);
  }
}
