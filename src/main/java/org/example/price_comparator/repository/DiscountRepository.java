package org.example.price_comparator.repository;

import org.example.price_comparator.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate startDate, LocalDate endDate);
}

