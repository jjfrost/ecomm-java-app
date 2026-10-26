package com.ecommerce.returns.model;

public enum ReturnStatus {
    REQUESTED,
    AUTHORIZED,
    IN_TRANSIT,
    RECEIVED,
    INSPECTED,
    COMPLETED,
    REJECTED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }
}
