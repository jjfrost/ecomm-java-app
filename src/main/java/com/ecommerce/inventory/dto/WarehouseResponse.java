package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.domain.WarehouseStatus;

import java.time.Instant;

public record WarehouseResponse(
        Long warehouseId,
        String warehouseCode,
        String name,
        String location,
        Integer capacity,
        WarehouseStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
