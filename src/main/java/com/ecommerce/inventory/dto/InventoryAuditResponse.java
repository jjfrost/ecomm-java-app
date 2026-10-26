package com.ecommerce.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryAuditResponse(
        Long auditId,
        Long inventoryId,
        String operation,
        BigDecimal oldQuantityOnHand,
        BigDecimal newQuantityOnHand,
        BigDecimal oldQuantityReserved,
        BigDecimal newQuantityReserved,
        String changedBy,
        Instant changedAt) {
}
