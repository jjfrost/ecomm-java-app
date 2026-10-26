package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.domain.WarehouseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
        @NotBlank @Size(max = 30) String warehouseCode,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String location,
        @NotNull @Positive Integer capacity,
        @NotNull WarehouseStatus status) {
}
