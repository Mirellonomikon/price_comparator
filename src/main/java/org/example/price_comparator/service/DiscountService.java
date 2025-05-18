package org.example.price_comparator.service;

import org.example.price_comparator.dto.BestDiscountsDto;

import java.time.LocalDate;
import java.util.List;

public interface DiscountService {

    List<BestDiscountsDto> getBestDiscounts(int limit);
    List<BestDiscountsDto> getBestDiscountsByDate(LocalDate date, int limit);
}

