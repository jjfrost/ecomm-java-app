package com.ecommerce.pricing.dto;

public record CouponValidationResponse(
        String code,
        Long promotionId,
        boolean valid,
        String reason) {
}
