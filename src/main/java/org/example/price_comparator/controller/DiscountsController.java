package org.example.price_comparator.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.BestDiscountsDto;
import org.example.price_comparator.service.DiscountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@Slf4j
public class DiscountsController {

    private final DiscountService discountService;

    public DiscountsController(DiscountService discountService) {
        this.discountService = discountService;
    }

    //For current date
    @GetMapping("/best")
    public ResponseEntity<List<BestDiscountsDto>> getBestDiscounts(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching best discounts with limit: {}", limit);
        List<BestDiscountsDto> bestDiscounts = discountService.getBestDiscounts(limit);
        return ResponseEntity.ok(bestDiscounts);
    }

    @GetMapping("/best/by-date")
    public ResponseEntity<List<BestDiscountsDto>> getBestDiscountsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching best discounts for date: {} with limit: {}", date, limit);
        List<BestDiscountsDto> bestDiscounts = discountService.getBestDiscountsByDate(date, limit);
        return ResponseEntity.ok(bestDiscounts);
    }
}


