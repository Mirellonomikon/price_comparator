package org.example.price_comparator.repository;

import org.example.price_comparator.model.PriceAlert;
import org.example.price_comparator.model.Product;
import org.example.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findByUserEmail(String userEmail);
    Optional<PriceAlert> findByUserEmailAndProductAndStore(String userEmail, Product product, Store store);
}
