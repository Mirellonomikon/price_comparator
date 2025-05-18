package org.example.price_comparator.repository;

import org.example.price_comparator.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, Long> {

}

