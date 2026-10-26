package com.ecommerce.inventory.domain;

public enum StockMovementType {
    RECEIPT,
    ADJUSTMENT_INCREASE,
    ADJUSTMENT_DECREASE,
    RESERVATION,
    RELEASE,
    SALE,
    RETURN,
    TRANSFER
}
