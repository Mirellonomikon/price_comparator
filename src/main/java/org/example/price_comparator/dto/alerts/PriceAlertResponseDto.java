package org.example.price_comparator.dto.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertResponseDto {

    private Long id;
    private String userEmail;
    private String productId;
    private String productName;
    private String storeName;
    private Double targetPrice;
    private Double currentBestPrice;
    private String currency;
    private String status;
    private Boolean isTriggered;
    private LocalDate createdDate;
    private LocalDate lastCheckedDate;
}
