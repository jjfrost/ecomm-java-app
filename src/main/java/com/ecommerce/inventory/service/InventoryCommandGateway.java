package com.ecommerce.inventory.service;

import com.ecommerce.inventory.domain.Inventory;
import com.ecommerce.inventory.domain.InventoryReservation;

import java.math.BigDecimal;
import java.time.Instant;

public interface InventoryCommandGateway {
    void receiveStock(
            Inventory inventory,
            BigDecimal quantity,
            String reference,
            String performedBy);

    void adjustStock(
            Inventory inventory,
            BigDecimal quantityDelta,
            String reason,
            String performedBy);

    Long reserveStock(
            Inventory inventory,
            Long orderId,
            BigDecimal quantity,
            Instant expiresAt,
            String performedBy);

    void releaseStock(
            InventoryReservation reservation,
            String reason,
            String performedBy);
}
