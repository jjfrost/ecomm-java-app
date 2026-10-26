package com.ecommerce.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryResponse(
        Long inventoryId,
        Long productId,
        BigDecimal quantityOnHand,
        BigDecimal quantityReserved,
        BigDecimal quantityAvailable,
        BigDecimal reorderLevel,
        BigDecimal maximumStock,
        boolean reorderRequired,
        String location,
        Instant lastUpdated,
        Instant createdAt) {
}
