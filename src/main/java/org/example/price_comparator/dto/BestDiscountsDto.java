package org.example.price_comparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestDiscountsDto {
    private String productId;
    private String productName;
    private String productCategory;
    private String brand;
    private Double packageQuantity;
    private String packageUnit;
    private String storeName;
    private Double percentageOfDiscount;
    private Double originalPrice;
    private Double discountedPrice;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
}

