package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record StockReservationRequest(
        @NotNull @Positive Long orderId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity,
        @Future Instant expiresAt,
        @NotBlank @Size(max = 100) String performedBy) {
}
