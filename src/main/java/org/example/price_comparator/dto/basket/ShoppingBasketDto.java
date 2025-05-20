package org.example.price_comparator.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingBasketDto {
    private List<BasketItemDto> items;
}
