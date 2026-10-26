package com.ecommerce.pricing.dto;

import com.ecommerce.pricing.domain.PromotionRuleType;
import com.ecommerce.pricing.domain.RuleOperator;

import java.time.Instant;

public record PromotionRuleResponse(
        Long promotionRuleId,
        Long promotionId,
        PromotionRuleType ruleType,
        RuleOperator operator,
        String value,
        Instant createdAt) {
}
