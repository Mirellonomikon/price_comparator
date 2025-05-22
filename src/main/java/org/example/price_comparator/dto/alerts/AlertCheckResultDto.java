package org.example.price_comparator.dto.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AlertCheckResultDto {
    private int totalAlerts;
    private int triggeredAlerts;
    private List<PriceAlertResponseDto> newlyTriggeredAlerts;
}