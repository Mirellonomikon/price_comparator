package org.example.price_comparator.service;

import org.example.price_comparator.dto.basket.OptimizedShoppingPlanDto;
import org.example.price_comparator.dto.basket.ShoppingBasketDto;

import java.time.LocalDate;

public interface ShoppingBasketService {
    OptimizedShoppingPlanDto optimizeBasket(ShoppingBasketDto basket, LocalDate date);
}



