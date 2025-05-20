package org.example.price_comparator.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.basket.OptimizedShoppingPlanDto;
import org.example.price_comparator.dto.basket.ShoppingBasketDto;
import org.example.price_comparator.service.ShoppingBasketService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/shopping-basket")
@Slf4j
public class ShoppingBasketController {

    private final ShoppingBasketService shoppingBasketService;

    public ShoppingBasketController(ShoppingBasketService shoppingBasketService) {
        this.shoppingBasketService = shoppingBasketService;
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizedShoppingPlanDto> optimizeBasket(@RequestBody ShoppingBasketDto basket) {
        log.info("Optimizing shopping basket with {} items for current date",
                basket.getItems() != null ? basket.getItems().size() : 0);

        OptimizedShoppingPlanDto plan = shoppingBasketService.optimizeBasket(basket, LocalDate.now());
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/optimize/by-date")
    public ResponseEntity<OptimizedShoppingPlanDto> optimizeBasketByDate(
            @RequestBody ShoppingBasketDto basket,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Optimizing shopping basket with {} items for date: {}",
                basket.getItems() != null ? basket.getItems().size() : 0, date);

        OptimizedShoppingPlanDto plan = shoppingBasketService.optimizeBasket(basket, date);
        return ResponseEntity.ok(plan);
    }
}


