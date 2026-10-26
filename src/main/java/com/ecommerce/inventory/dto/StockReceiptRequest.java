package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockReceiptRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity,
        @NotBlank @Size(max = 100) String reference,
        @NotBlank @Size(max = 100) String performedBy) {
}
