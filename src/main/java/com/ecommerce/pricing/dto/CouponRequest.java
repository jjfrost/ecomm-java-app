package com.ecommerce.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CouponRequest(
        @NotBlank @Size(max = 50) String code,
        @NotNull @Positive Long promotionId,
        @NotNull Instant validFrom,
        @NotNull Instant validTo,
        @Positive Long usageLimit,
        @Positive Integer perCustomerLimit,
        boolean active) {
}
