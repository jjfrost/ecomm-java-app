package com.ecommerce.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "payment_attempts", schema = "ecomm")
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_attempt_seq")
    @SequenceGenerator(
            name = "payment_attempt_seq",
            sequenceName = "ecomm.payment_attempt_seq",
            allocationSize = 1)
    @Column(name = "payment_attempt_id")
    private Long paymentAttemptId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status", nullable = false, length = 30)
    private PaymentAttemptStatus attemptStatus;

    @Column(name = "processor_transaction_id", length = 150)
    private String processorTransactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    protected PaymentAttempt() {
    }

    public Long getPaymentAttemptId() {
        return paymentAttemptId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentAttemptStatus getAttemptStatus() {
        return attemptStatus;
    }

    public String getProcessorTransactionId() {
        return processorTransactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }
}
