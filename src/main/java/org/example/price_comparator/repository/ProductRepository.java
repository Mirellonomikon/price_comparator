package org.example.price_comparator.repository;

import org.example.price_comparator.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByProductCategoryIgnoreCase(String category);
    List<Product> findByProductCategoryAndProductNameContainingIgnoreCase(String category, String productName);
    List<Product> findByBrandIgnoreCase(String brand);
}
