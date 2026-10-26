package com.ecommerce.pricing.repository;

import com.ecommerce.pricing.domain.PromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRuleRepository extends JpaRepository<PromotionRule, Long> {
    List<PromotionRule> findByPromotionIdOrderByPromotionRuleId(Long promotionId);
    boolean existsByPromotionId(Long promotionId);
}
