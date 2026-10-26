package com.ecommerce.fulfillment.model;

public enum ShipmentStatus {
    PENDING,
    READY,
    SHIPPED,
    IN_TRANSIT,
    DELIVERED,
    EXCEPTION,
    CANCELLED;

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
