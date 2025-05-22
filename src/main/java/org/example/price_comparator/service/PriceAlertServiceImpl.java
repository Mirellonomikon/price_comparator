package org.example.price_comparator.service;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.alerts.AlertCheckResultDto;
import org.example.price_comparator.dto.alerts.PriceAlertRequestDto;
import org.example.price_comparator.dto.alerts.PriceAlertResponseDto;
import org.example.price_comparator.exceptions.PriceAlertException;
import org.example.price_comparator.model.*;
import org.example.price_comparator.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PriceAlertServiceImpl implements PriceAlertService {

    private final PriceAlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final PriceRepository priceRepository;
    private final DiscountRepository discountRepository;

    public PriceAlertServiceImpl(PriceAlertRepository alertRepository, ProductRepository productRepository,
                                 StoreRepository storeRepository, PriceRepository priceRepository, DiscountRepository discountRepository) {
        this.alertRepository = alertRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public PriceAlertResponseDto createAlert(PriceAlertRequestDto alertRequest) {
        try {
            // Validate product
            Product product = productRepository.findById(alertRequest.getProductId())
                    .orElseThrow(() -> new PriceAlertException("Product not found: " + alertRequest.getProductId()));

            // Validate store if provided
            Store store = null;
            if (alertRequest.getStoreName() != null && !alertRequest.getStoreName().isEmpty()) {
                store = storeRepository.findByName(alertRequest.getStoreName())
                        .orElseThrow(() -> new PriceAlertException("Store not found: " + alertRequest.getStoreName()));
            }

            // Check if alert already exists
            Optional<PriceAlert> existingAlert = alertRepository.findByUserEmailAndProductAndStore(
                    alertRequest.getUserEmail(), product, store);

            if (existingAlert.isPresent()) {
                PriceAlert alert = existingAlert.get();
                // Update existing alert if it's not active
                if (alert.getStatus() != PriceAlert.AlertStatus.ACTIVE) {
                    alert.setTargetPrice(alertRequest.getTargetPrice());
                    alert.setStatus(PriceAlert.AlertStatus.ACTIVE);
                    alert.setLastCheckedDate(LocalDate.now());
                    alertRepository.save(alert);
                    return mapToResponseDto(alert, getCurrentBestPrice(product, store));
                } else {
                    throw new PriceAlertException("Alert already exists for this product and store");
                }
            }

            // Find current best price to get currency
            Double currentPrice = getCurrentBestPrice(product, store);
            String currency = "USD"; // Default

            // Try to find a price to get the currency
            if (store != null) {
                Optional<Price> latestPrice = priceRepository.findTopByProductAndStoreOrderByDateDesc(product, store);
                if (latestPrice.isPresent()) {
                    currency = latestPrice.get().getCurrency();
                }
            } else {
                List<Price> prices = priceRepository.findByProductOrderByDateDesc(product);
                if (!prices.isEmpty()) {
                    currency = prices.getFirst().getCurrency();
                }
            }

            PriceAlert alert = new PriceAlert();
            alert.setUserEmail(alertRequest.getUserEmail());
            alert.setProduct(product);
            alert.setStore(store);
            alert.setTargetPrice(alertRequest.getTargetPrice());
            alert.setCurrency(currency);
            alert.setStatus(PriceAlert.AlertStatus.ACTIVE);
            alert.setCreatedDate(LocalDate.now());
            alert.setLastCheckedDate(LocalDate.now());

            // Check if alert is already triggered
            if (currentPrice != null && currentPrice <= alertRequest.getTargetPrice()) {
                alert.setStatus(PriceAlert.AlertStatus.TRIGGERED);
            }

            alertRepository.save(alert);
            return mapToResponseDto(alert, currentPrice);

        } catch (PriceAlertException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating price alert", e);
            throw new PriceAlertException("Error creating price alert: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PriceAlertResponseDto> getUserAlerts(String userEmail) {
        try {
            List<PriceAlert> alerts = alertRepository.findByUserEmail(userEmail);

            return alerts.stream()
                    .map(alert -> {
                        Double currentPrice = getCurrentBestPrice(alert.getProduct(), alert.getStore());
                        return mapToResponseDto(alert, currentPrice);
                    }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting user alerts", e);
            throw new PriceAlertException("Error getting user alerts: " + e.getMessage(), e);
        }
    }

    @Override
    public PriceAlertResponseDto getAlertById(Long alertId) {
        try {
            PriceAlert alert = alertRepository.findById(alertId)
                    .orElseThrow(() -> new PriceAlertException("Alert not found: " + alertId));

            Double currentPrice = getCurrentBestPrice(alert.getProduct(), alert.getStore());
            return mapToResponseDto(alert, currentPrice);

        } catch (PriceAlertException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting alert by ID", e);
            throw new PriceAlertException("Error getting alert: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAlert(Long alertId) {
        try {
            PriceAlert alert = alertRepository.findById(alertId)
                    .orElseThrow(() -> new PriceAlertException("Alert not found: " + alertId));

            alertRepository.delete(alert);

        } catch (PriceAlertException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting alert", e);
            throw new PriceAlertException("Error deleting alert: " + e.getMessage(), e);
        }
    }

    @Override
    public AlertCheckResultDto checkAlerts(String userEmail) {
        try {
            List<PriceAlert> alerts;
            if (userEmail != null && !userEmail.isEmpty()) {
                alerts = alertRepository.findByUserEmail(userEmail);
            } else {
                alerts = alertRepository.findAll();
            }

            List<PriceAlertResponseDto> newlyTriggeredAlerts = new ArrayList<>();
            int triggeredCount = 0;

            for (PriceAlert alert : alerts) {
                Double currentPrice = getCurrentBestPrice(alert.getProduct(), alert.getStore());

                // Skip if we can't determine current price
                if (currentPrice == null) {
                    continue;
                }

                // Update last checked date
                alert.setLastCheckedDate(LocalDate.now());

                // Check if price has dropped below target
                if (currentPrice <= alert.getTargetPrice()) {
                    // Only count as newly triggered if it wasn't triggered before
                    if (alert.getStatus() != PriceAlert.AlertStatus.TRIGGERED) {
                        alert.setStatus(PriceAlert.AlertStatus.TRIGGERED);
                        newlyTriggeredAlerts.add(mapToResponseDto(alert, currentPrice));
                    }
                    triggeredCount++;
                }

                alertRepository.save(alert);
            }

            AlertCheckResultDto result = new AlertCheckResultDto();
            result.setTotalAlerts(alerts.size());
            result.setTriggeredAlerts(triggeredCount);
            result.setNewlyTriggeredAlerts(newlyTriggeredAlerts);

            return result;

        } catch (Exception e) {
            log.error("Error checking alerts", e);
            throw new PriceAlertException("Error checking alerts: " + e.getMessage(), e);
        }
    }

    private Double getCurrentBestPrice(Product product, Store store) {
        LocalDate today = LocalDate.now();

        if (store != null) {
            // Get best price for specific store
            Optional<Price> latestPrice = priceRepository.findTopByProductAndStoreOrderByDateDesc(product, store);

            if (latestPrice.isPresent()) {
                Price price = latestPrice.get();
                double originalPrice = price.getPrice();

                Optional<Discount> activeDiscount = discountRepository
                        .findByProductAndStoreAndStartDateLessThanEqualAndEndDateGreaterThanEqual(product, store, today, today);

                if (activeDiscount.isPresent()) {
                    double discountPercentage = activeDiscount.get().getPercentageOfDiscount();
                    return roundToTwoDecimals(originalPrice * (1 - discountPercentage / 100));
                }

                return roundToTwoDecimals(originalPrice);
            }

            return null;
        } else {
            // Get best price across all stores
            List<Price> latestPrices = priceRepository.findByProductOrderByDateDesc(product);

            if (latestPrices.isEmpty()) {
                return null;
            }

            // Group by store to get the most recent price for each store
            Map<Store, Price> latestPriceByStore = new HashMap<>();
            for (Price price : latestPrices) {
                Store priceStore = price.getStore();
                if (!latestPriceByStore.containsKey(priceStore) || price.getDate().isAfter(latestPriceByStore.get(priceStore).getDate())) {
                    latestPriceByStore.put(priceStore, price);
                }
            }

            // Find the best price considering discounts
            Double bestPrice = null;

            for (Map.Entry<Store, Price> entry : latestPriceByStore.entrySet()) {
                Store priceStore = entry.getKey();
                Price price = entry.getValue();
                double originalPrice = price.getPrice();
                double finalPrice = originalPrice;

                Optional<Discount> activeDiscount = discountRepository
                        .findByProductAndStoreAndStartDateLessThanEqualAndEndDateGreaterThanEqual(product, priceStore, today, today);

                if (activeDiscount.isPresent()) {
                    double discountPercentage = activeDiscount.get().getPercentageOfDiscount();
                    finalPrice = originalPrice * (1 - discountPercentage / 100);
                }

                // Update best price if the new one is lower
                if (bestPrice == null || finalPrice < bestPrice) {
                    bestPrice = finalPrice;
                }
            }

            return bestPrice != null ? roundToTwoDecimals(bestPrice) : null;
        }
    }

    private PriceAlertResponseDto mapToResponseDto(PriceAlert alert, Double currentPrice) {
        PriceAlertResponseDto dto = new PriceAlertResponseDto();
        dto.setId(alert.getId());
        dto.setUserEmail(alert.getUserEmail());
        dto.setProductId(alert.getProduct().getId());
        dto.setProductName(alert.getProduct().getProductName());
        dto.setStoreName(alert.getStore() != null ? alert.getStore().getName() : null);
        dto.setTargetPrice(roundToTwoDecimals(alert.getTargetPrice()));
        dto.setCurrentBestPrice(currentPrice != null ? roundToTwoDecimals(currentPrice) : null);
        dto.setCurrency(alert.getCurrency());
        dto.setStatus(alert.getStatus().toString());
        dto.setIsTriggered(alert.getStatus() == PriceAlert.AlertStatus.TRIGGERED);
        dto.setCreatedDate(alert.getCreatedDate());
        dto.setLastCheckedDate(alert.getLastCheckedDate());
        return dto;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
