package org.example.price_comparator.service;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.BestDiscountsDto;
import org.example.price_comparator.exceptions.DiscountProcessingException;
import org.example.price_comparator.model.Discount;
import org.example.price_comparator.model.Price;
import org.example.price_comparator.repository.DiscountRepository;
import org.example.price_comparator.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final PriceRepository priceRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository, PriceRepository priceRepository) {
        this.discountRepository = discountRepository;
        this.priceRepository = priceRepository;
    }

    @Override
    public List<BestDiscountsDto> getBestDiscounts(int limit) {
        return getBestDiscountsByDate(LocalDate.now(), limit);
    }

    // Active discounts for the given date
    @Override
    public List<BestDiscountsDto> getBestDiscountsByDate(LocalDate date, int limit) {
        try {
            List<Discount> activeDiscounts = discountRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
            if (activeDiscounts.isEmpty()) {
                log.info("No active discounts found for date: {}", date);
                return Collections.emptyList();
            }

            List<BestDiscountsDto> bestDiscountsDtos = mapDiscountsToDtos(activeDiscounts, date);
            // Sort by discount percentage and limit the results
            return bestDiscountsDtos.stream()
                    .sorted(Comparator.comparing(BestDiscountsDto::getPercentageOfDiscount).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error processing best discounts for date: {}", date, e);
            throw new DiscountProcessingException("Error processing best discounts: " + e.getMessage(), e);
        }
    }

    // Last discounts in 24 hours
    @Override
    public List<BestDiscountsDto> getNewDiscountsLast24Hours() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return getNewDiscountsAfterDate(yesterday);
    }

    // Last discounts in 24 hours (by day)
    @Override
    public List<BestDiscountsDto> getNewDiscountsAfterDateLast24Hours(LocalDate date) {
        try {
            // 24 hours after the start date
            LocalDate nextDay = date.plusDays(1);

            // Discounts that started within the 24-hour window
            List<Discount> newDiscounts = discountRepository.findByStartDateBetweenOrderByStartDateDesc(date, nextDay);

            if (newDiscounts.isEmpty()) {
                log.info("No new discounts found within 24 hours of date: {}", date);
                return Collections.emptyList();
            }

            List<BestDiscountsDto> newDiscountsDtos = mapDiscountsToDtos(newDiscounts, nextDay);

            return newDiscountsDtos.stream()
                    .sorted(Comparator.comparing(BestDiscountsDto::getStartDate).reversed())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error processing new discounts within 24 hours of date: {}", date, e);
            throw new DiscountProcessingException("Error processing new discounts: " + e.getMessage(), e);
        }
    }

    // Discounts that start on or after the given date
    @Override
    public List<BestDiscountsDto> getNewDiscountsAfterDate(LocalDate date) {
        try {
            List<Discount> newDiscounts = discountRepository.findByStartDateGreaterThanEqualOrderByStartDateDesc(date);
            if (newDiscounts.isEmpty()) {
                log.info("No new discounts found after date: {}", date);
                return Collections.emptyList();
            }

            LocalDate today = LocalDate.now();
            List<BestDiscountsDto> newDiscountsDtos = mapDiscountsToDtos(newDiscounts, today);
            return newDiscountsDtos.stream()
                    .sorted(Comparator.comparing(BestDiscountsDto::getStartDate).reversed())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error processing new discounts after date: {}", date, e);
            throw new DiscountProcessingException("Error processing new discounts: " + e.getMessage(), e);
        }
    }

    private List<BestDiscountsDto> mapDiscountsToDtos(List<Discount> discounts, LocalDate priceDate) {
        List<BestDiscountsDto> dtos = new ArrayList<>();
        for (Discount discount : discounts) {
            Optional<Price> priceOpt = priceRepository.findTopByProductAndStoreAndDateLessThanEqualOrderByDateDesc(
                    discount.getProduct(), discount.getStore(), priceDate);
            if (priceOpt.isPresent()) {
                Price price = priceOpt.get();
                Double originalPrice = price.getPrice();
                Double discountedPrice = originalPrice * (1 - discount.getPercentageOfDiscount() / 100);
                BestDiscountsDto dto = new BestDiscountsDto();
                dto.setProductId(price.getProduct().getId());
                dto.setProductName(price.getProduct().getProductName());
                dto.setProductCategory(price.getProduct().getProductCategory());
                dto.setBrand(price.getProduct().getBrand());
                dto.setPackageQuantity(price.getProduct().getPackageQuantity());
                dto.setPackageUnit(price.getProduct().getPackageUnit());
                dto.setStoreName(price.getStore().getName());
                dto.setPercentageOfDiscount(discount.getPercentageOfDiscount());
                dto.setOriginalPrice(originalPrice);
                dto.setDiscountedPrice(discountedPrice);
                dto.setCurrency(price.getCurrency());
                dto.setStartDate(discount.getStartDate());
                dto.setEndDate(discount.getEndDate());
                dtos.add(dto);
            }
        }
        return dtos;
    }
}


