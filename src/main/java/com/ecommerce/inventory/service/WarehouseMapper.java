package com.ecommerce.inventory.service;

import com.ecommerce.inventory.domain.Warehouse;
import com.ecommerce.inventory.dto.WarehouseRequest;
import com.ecommerce.inventory.dto.WarehouseResponse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {
    public Warehouse toEntity(WarehouseRequest request) {
        return Warehouse.create(
                normalizeCode(request.warehouseCode()),
                request.name().trim(),
                request.location().trim(),
                request.capacity(),
                request.status());
    }

    public void update(Warehouse warehouse, WarehouseRequest request) {
        warehouse.applyChanges(
                normalizeCode(request.warehouseCode()),
                request.name().trim(),
                request.location().trim(),
                request.capacity(),
                request.status());
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getWarehouseId(),
                warehouse.getWarehouseCode(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getCapacity(),
                warehouse.getStatus(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt());
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
