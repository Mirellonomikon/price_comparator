package org.example.price_comparator.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreShoppingListDto {
    private String storeName;
    private List<OptimizedItemDto> items;
    private Double subtotal;
    private String currency;
}
