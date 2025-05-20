package org.example.price_comparator.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizedShoppingPlanDto {
    private List<StoreShoppingListDto> storeLists;
    private Double totalCost;
    private Double worstCaseCost;
    private Double totalSavings;
    private String currency;
    private Integer totalStores;
    private Integer totalItems;
}
