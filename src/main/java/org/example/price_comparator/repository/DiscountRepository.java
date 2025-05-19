package org.example.price_comparator.repository;

import org.example.price_comparator.model.Discount;
import org.example.price_comparator.model.Product;
import org.example.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate startDate, LocalDate endDate);
    List<Discount> findByStartDateGreaterThanEqualOrderByStartDateDesc(LocalDate date);
    List<Discount> findByStartDateBetweenOrderByStartDateDesc(LocalDate startDate, LocalDate endDate);
    Optional<Discount> findByProductAndStoreAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Product product, Store store, LocalDate startDate, LocalDate endDate);
}

