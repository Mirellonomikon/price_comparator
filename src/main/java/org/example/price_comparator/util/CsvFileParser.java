package org.example.price_comparator.util;

import java.time.LocalDate;

public class CsvFileParser {

    public static String extractStoreName(String fileName) {
        String[] parts = fileName.split("_");
        return parts[0];
    }

    public static LocalDate extractDate(String fileName) {
        String[] parts = fileName.split("_");
        String dateStr;

        if (parts.length == 2) {
            dateStr = parts[1].replace(".csv", "");
        } else {
            // Discount files
            dateStr = parts[2].replace(".csv", "");
        }

        return LocalDate.parse(dateStr);
    }

    public static boolean isDiscountFile(String fileName) {
        return fileName.contains("_discounts_");
    }
}

