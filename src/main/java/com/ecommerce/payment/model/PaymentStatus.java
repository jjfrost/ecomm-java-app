package com.ecommerce.payment.model;

public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    CAPTURED,
    SETTLED,
    FAILED,
    VOIDED,
    PARTIALLY_REFUNDED,
    REFUNDED;

    public boolean permitsRefund() {
        return this == CAPTURED || this == SETTLED || this == PARTIALLY_REFUNDED;
    }
}
