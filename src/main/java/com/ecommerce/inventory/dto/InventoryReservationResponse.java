package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.domain.InventoryReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryReservationResponse(
        Long reservationId,
        Long inventoryId,
        Long orderId,
        BigDecimal quantity,
        InventoryReservationStatus status,
        Instant expiresAt,
        Instant createdAt,
        Instant releasedAt) {
}
