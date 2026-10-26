package com.ecommerce.inventory.service;

import com.ecommerce.inventory.domain.Inventory;
import com.ecommerce.inventory.domain.InventoryAuditLog;
import com.ecommerce.inventory.domain.InventoryReservation;
import com.ecommerce.inventory.domain.StockMovement;
import com.ecommerce.inventory.dto.InventoryAuditResponse;
import com.ecommerce.inventory.dto.InventoryReservationResponse;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.StockMovementResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    public InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getInventoryId(),
                inventory.getProductId(),
                inventory.getQuantityOnHand(),
                inventory.getQuantityReserved(),
                inventory.availableQuantity(),
                inventory.getReorderLevel(),
                inventory.getMaximumStock(),
                inventory.requiresReorder(),
                inventory.getLocation(),
                inventory.getLastUpdated(),
                inventory.getCreatedAt());
    }

    public InventoryReservationResponse toResponse(InventoryReservation reservation) {
        return new InventoryReservationResponse(
                reservation.getReservationId(),
                reservation.getInventoryId(),
                reservation.getOrderId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getExpiresAt(),
                reservation.getCreatedAt(),
                reservation.getReleasedAt());
    }

    public StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getMovementId(),
                movement.getInventoryId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getReason(),
                movement.getPerformedBy(),
                movement.getCreatedAt());
    }

    public InventoryAuditResponse toResponse(InventoryAuditLog audit) {
        return new InventoryAuditResponse(
                audit.getAuditId(),
                audit.getInventoryId(),
                audit.getOperation(),
                audit.getOldQuantityOnHand(),
                audit.getNewQuantityOnHand(),
                audit.getOldQuantityReserved(),
                audit.getNewQuantityReserved(),
                audit.getChangedBy(),
                audit.getChangedAt());
    }
}
