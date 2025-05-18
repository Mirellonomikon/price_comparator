package org.example.price_comparator.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productCategory;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Double packageQuantity;

    @Column(nullable = false)
    private String packageUnit;
}
