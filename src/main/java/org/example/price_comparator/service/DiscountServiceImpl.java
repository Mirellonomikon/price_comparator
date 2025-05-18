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

    @Override
    public List<BestDiscountsDto> getBestDiscountsByDate(LocalDate date, int limit) {
        try {
            List<Discount> activeDiscounts = discountRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
            if (activeDiscounts.isEmpty()) {
                log.info("No active discounts found for date: {}", date);
                return Collections.emptyList();
            }
            // Map discounts to DTOs with price information
            List<BestDiscountsDto> bestDiscountsDtos = new ArrayList<>();
            for (Discount discount : activeDiscounts) {
                // Find the most recent price for this product at this store
                Optional<Price> priceOpt = priceRepository.findTopByProductAndStoreAndDateLessThanEqualOrderByDateDesc(
                        discount.getProduct(), discount.getStore(), date);
                if (priceOpt.isPresent()) {
                    BestDiscountsDto dto = getBestDiscountsDto(discount, priceOpt);
                    bestDiscountsDtos.add(dto);
                } else {
                    log.warn("No price found for product {} in store {}, skipping...",
                            discount.getProduct().getId(), discount.getStore().getName());
                }
            }
            // Sort by discount percentage and limit to top results
            return bestDiscountsDtos.stream()
                    .sorted(Comparator.comparing(BestDiscountsDto::getPercentageOfDiscount).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new DiscountProcessingException("Error processing best discounts: " + e.getMessage(), e);
        }
    }

    private BestDiscountsDto getBestDiscountsDto(Discount discount, Optional<Price> priceOpt) {
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
            return dto;
        }
        return null;
    }
}


