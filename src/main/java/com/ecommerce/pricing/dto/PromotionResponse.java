package com.ecommerce.pricing.dto;

import com.ecommerce.pricing.domain.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

public record PromotionResponse(
        Long promotionId,
        String name,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        Instant validFrom,
        Instant validTo,
        boolean active,
        Integer priority,
        Long usageLimit,
        Long usageCount,
        Instant createdAt,
        Instant updatedAt) {
}
