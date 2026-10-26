package com.ecommerce.pricing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record PriceListRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @NotNull Instant validFrom,
        Instant validTo,
        boolean active,
        @Min(0) @Max(10000) int priority) {
}
