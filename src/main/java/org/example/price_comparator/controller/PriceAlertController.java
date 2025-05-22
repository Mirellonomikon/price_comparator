package org.example.price_comparator.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.alerts.AlertCheckResultDto;
import org.example.price_comparator.dto.alerts.PriceAlertRequestDto;
import org.example.price_comparator.dto.alerts.PriceAlertResponseDto;
import org.example.price_comparator.service.PriceAlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price-alerts")
@Slf4j
public class PriceAlertController {

    private final PriceAlertService alertService;

    public PriceAlertController(PriceAlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/add")
    public ResponseEntity<PriceAlertResponseDto> createAlert(@RequestBody PriceAlertRequestDto alertRequest) {
        log.info("Creating price alert for product: {}, user: {}", alertRequest.getProductId(), alertRequest.getUserEmail());
        PriceAlertResponseDto alert = alertService.createAlert(alertRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<PriceAlertResponseDto>> getUserAlerts(@PathVariable String email) {
        log.info("Getting price alerts for user: {}", email);
        List<PriceAlertResponseDto> alerts = alertService.getUserAlerts(email);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceAlertResponseDto> getAlertById(@PathVariable Long id) {
        log.info("Getting price alert by ID: {}", id);
        PriceAlertResponseDto alert = alertService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        log.info("Deleting price alert with ID: {}", id);
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    public ResponseEntity<AlertCheckResultDto> checkAllAlerts() {
        log.info("Checking all price alerts");
        AlertCheckResultDto result = alertService.checkAlerts(null);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check/{email}")
    public ResponseEntity<AlertCheckResultDto> checkUserAlerts(@PathVariable String email) {
        log.info("Checking price alerts for user: {}", email);
        AlertCheckResultDto result = alertService.checkAlerts(email);
        return ResponseEntity.ok(result);
    }
}
