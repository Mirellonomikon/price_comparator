package org.example.price_comparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendationDto {
    private String productId;
    private String productName;
    private String productCategory;
    private String brand;
    private Double packageQuantity;
    private String packageUnit;
    private String storeName;
    private Double price;
    private String currency;
    private Double valuePerUnit;
    private String unitType;
    private LocalDate priceDate;
    private Boolean onDiscount;
    private Double percentageOfDiscount;
    private Double originalPrice;
    private Double discountedPrice;
}




