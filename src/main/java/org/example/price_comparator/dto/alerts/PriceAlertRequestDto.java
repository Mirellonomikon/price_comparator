package org.example.price_comparator.dto.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertRequestDto {

    private String productId;
    private String storeName;
    private Double targetPrice;
    private String userEmail;
}
