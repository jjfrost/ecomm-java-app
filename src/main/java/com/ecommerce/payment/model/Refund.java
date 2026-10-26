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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "refunds", schema = "ecomm")
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refund_seq")
    @SequenceGenerator(
            name = "refund_seq",
            sequenceName = "ecomm.refund_seq",
            allocationSize = 1)
    @Column(name = "refund_id")
    private Long refundId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 30)
    private RefundStatus refundStatus;

    @Column(name = "processor_refund_id", length = 150)
    private String processorRefundId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected Refund() {
    }

    public Long getRefundId() {
        return refundId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public RefundStatus getRefundStatus() {
        return refundStatus;
    }

    public String getProcessorRefundId() {
        return processorRefundId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
