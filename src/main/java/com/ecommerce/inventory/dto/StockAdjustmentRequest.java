package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StockAdjustmentRequest(
        @NotNull @DecimalMin("-1000000.00") @DecimalMax("1000000.00") BigDecimal quantityDelta,
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Size(max = 100) String performedBy) {
}
