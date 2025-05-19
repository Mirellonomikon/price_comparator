package org.example.price_comparator.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CsvProcessingException.class)
    public ResponseEntity<String> handleCsvProcessingException(CsvProcessingException ex) {
        log.error("CSV Processing Exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing CSV file. " + ex.getMessage());
    }

    @ExceptionHandler(CsvFolderNotFoundException.class)
    public ResponseEntity<String> handleCsvFolderNotFoundException(CsvFolderNotFoundException ex) {
        log.error("CSV Folder Not Found Exception: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder not found. " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        log.error("Exception caught: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
    }

    @ExceptionHandler(DiscountProcessingException.class)
    public ResponseEntity<String> handleDiscountProcessingException(DiscountProcessingException ex) {
        log.error("Discount Processing Exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing discounts. " + ex.getMessage());
    }

    @ExceptionHandler(ProductRecommendationException.class)
    public ResponseEntity<String> handleProductRecommendationException(ProductRecommendationException ex) {
        log.error("Product Recommendation Exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing product recommendations: " + ex.getMessage());
    }

    @ExceptionHandler(PriceHistoryException.class)
    public ResponseEntity<String> handlePriceHistoryException(PriceHistoryException ex) {
        log.error("Price History Exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing price history: " + ex.getMessage());
    }

}
