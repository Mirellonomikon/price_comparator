package org.example.price_comparator.exceptions;

public class CsvFolderNotFoundException extends RuntimeException {

    public CsvFolderNotFoundException(String message) {
        super(message);
    }

    public CsvFolderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
