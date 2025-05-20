package org.example.price_comparator.dto.basket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizedItemDto {
    private String productId;
    private String productName;
    private String brand;
    private Double packageQuantity;
    private String packageUnit;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String currency;
    private Boolean onDiscount;
    private Double discountPercentage;
    private Double savingsPerUnit;
}
