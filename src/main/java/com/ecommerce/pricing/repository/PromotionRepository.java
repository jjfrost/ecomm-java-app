package com.ecommerce.pricing.repository;

import com.ecommerce.pricing.domain.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndPromotionIdNot(String name, Long promotionId);
    Page<Promotion> findByActiveTrue(Pageable pageable);
}
