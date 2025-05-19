package org.example.price_comparator.repository;

import org.example.price_comparator.model.Price;
import org.example.price_comparator.model.Product;
import org.example.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;


public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findTopByProductAndStoreAndDateLessThanEqualOrderByDateDesc(Product product, Store store, LocalDate date);
    List<Price> findByProductAndDateLessThanEqualOrderByDateDesc(Product product, LocalDate date);
    List<Price> findByProductAndDateBetweenOrderByDateAsc(Product product, LocalDate startDate, LocalDate endDate);
    List<Price> findByProductAndStoreAndDateBetweenOrderByDateAsc(Product product, Store store, LocalDate startDate, LocalDate endDate);
}

