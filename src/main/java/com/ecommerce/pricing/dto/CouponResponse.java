package com.ecommerce.pricing.dto;

import java.time.Instant;

public record CouponResponse(
        Long couponId,
        String code,
        Long promotionId,
        Instant validFrom,
        Instant validTo,
        Long usageLimit,
        Long usageCount,
        Integer perCustomerLimit,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
