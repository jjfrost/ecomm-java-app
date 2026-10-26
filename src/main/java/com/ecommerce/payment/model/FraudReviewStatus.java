package com.ecommerce.payment.model;

public enum FraudReviewStatus {
    PENDING,
    IN_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED;

    public boolean isOpen() {
        return this == PENDING || this == IN_REVIEW;
    }
}
