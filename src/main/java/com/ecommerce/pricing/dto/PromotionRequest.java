package com.ecommerce.pricing.dto;

import com.ecommerce.pricing.domain.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record PromotionRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        @NotNull Instant validFrom,
        @NotNull Instant validTo,
        boolean active,
        @Min(0) @Max(10000) int priority,
        @Positive Long usageLimit) {
}
