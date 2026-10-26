package com.ecommerce.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponValidationRequest(
        @NotBlank @Size(max = 50) String code,
        @NotNull @Positive Long customerId,
        @NotNull @DecimalMin("0.00") BigDecimal subtotal,
        Instant asOf) {
}
