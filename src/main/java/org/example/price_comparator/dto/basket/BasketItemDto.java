package org.example.price_comparator.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemDto {
    private String productId;
    private Integer quantity;
}
