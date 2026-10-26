package com.ecommerce.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceListItemRequest(
        @NotNull @Positive Long productId,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @DecimalMin("0.01") BigDecimal minimumQuantity,
        @NotNull Instant validFrom,
        Instant validTo) {
}
