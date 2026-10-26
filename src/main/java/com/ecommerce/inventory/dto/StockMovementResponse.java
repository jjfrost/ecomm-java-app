package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.domain.StockMovementType;

import java.math.BigDecimal;
import java.time.Instant;

public record StockMovementResponse(
        Long movementId,
        Long inventoryId,
        StockMovementType movementType,
        BigDecimal quantity,
        String referenceType,
        Long referenceId,
        String reason,
        String performedBy,
        Instant createdAt) {
}
