package com.ecommerce.pricing.dto;

import com.ecommerce.pricing.domain.PromotionRuleType;
import com.ecommerce.pricing.domain.RuleOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromotionRuleRequest(
        @NotNull PromotionRuleType ruleType,
        @NotNull RuleOperator operator,
        @NotBlank @Size(max = 500) String value) {
}
