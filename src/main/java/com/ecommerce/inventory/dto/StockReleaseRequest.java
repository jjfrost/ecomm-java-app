package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StockReleaseRequest(
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Size(max = 100) String performedBy) {
}
