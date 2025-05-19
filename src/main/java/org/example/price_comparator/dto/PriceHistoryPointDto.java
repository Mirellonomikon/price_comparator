package org.example.price_comparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryPointDto {
    private LocalDate date;
    private Double price;
    private String currency;
    private Boolean isDiscounted;
    private Double discountPercentage;
    private Double originalPrice;
}
