package org.example.price_comparator.service;

import org.example.price_comparator.dto.BestDiscountsDto;

import java.time.LocalDate;
import java.util.List;

public interface DiscountService {

    List<BestDiscountsDto> getBestDiscounts(int limit);
    List<BestDiscountsDto> getBestDiscountsByDate(LocalDate date, int limit);
    List<BestDiscountsDto> getNewDiscountsLast24Hours();
    List<BestDiscountsDto> getNewDiscountsAfterDateLast24Hours(LocalDate date);
    List<BestDiscountsDto> getNewDiscountsAfterDate(LocalDate date);
}

