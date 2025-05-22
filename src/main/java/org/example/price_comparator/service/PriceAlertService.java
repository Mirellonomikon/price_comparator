package org.example.price_comparator.service;

import org.example.price_comparator.dto.alerts.AlertCheckResultDto;
import org.example.price_comparator.dto.alerts.PriceAlertRequestDto;
import org.example.price_comparator.dto.alerts.PriceAlertResponseDto;

import java.util.List;

public interface PriceAlertService {

    PriceAlertResponseDto createAlert(PriceAlertRequestDto alertRequest);
    List<PriceAlertResponseDto> getUserAlerts(String userEmail);
    PriceAlertResponseDto getAlertById(Long alertId);
    void deleteAlert(Long alertId);
    AlertCheckResultDto checkAlerts(String userEmail);
}

