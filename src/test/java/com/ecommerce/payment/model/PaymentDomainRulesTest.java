package com.ecommerce.payment.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDomainRulesTest {
    @Test
    void onlyCapturedSettlementStatesPermitRefunds() {
        assertAll(
                () -> assertTrue(PaymentStatus.CAPTURED.permitsRefund()),
                () -> assertTrue(PaymentStatus.SETTLED.permitsRefund()),
                () -> assertTrue(PaymentStatus.PARTIALLY_REFUNDED.permitsRefund()),
                () -> assertFalse(PaymentStatus.PENDING.permitsRefund()),
                () -> assertFalse(PaymentStatus.AUTHORIZED.permitsRefund()),
                () -> assertFalse(PaymentStatus.FAILED.permitsRefund()),
                () -> assertFalse(PaymentStatus.VOIDED.permitsRefund()),
                () -> assertFalse(PaymentStatus.REFUNDED.permitsRefund()));
    }

    @Test
    void fraudReviewIsOpenOnlyBeforeAFinalDecision() {
        assertAll(
                () -> assertTrue(FraudReviewStatus.PENDING.isOpen()),
                () -> assertTrue(FraudReviewStatus.IN_REVIEW.isOpen()),
                () -> assertFalse(FraudReviewStatus.APPROVED.isOpen()),
                () -> assertFalse(FraudReviewStatus.REJECTED.isOpen()),
                () -> assertFalse(FraudReviewStatus.CANCELLED.isOpen()));
    }
}
