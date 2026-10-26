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
@Table(name = "fraud_review_queue", schema = "ecomm")
public class FraudReview {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fraud_review_seq")
    @SequenceGenerator(
            name = "fraud_review_seq",
            sequenceName = "ecomm.fraud_review_seq",
            allocationSize = 1)
    @Column(name = "fraud_review_id")
    private Long fraudReviewId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "risk_reason", nullable = false, length = 1000)
    private String riskReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 30)
    private FraudReviewStatus reviewStatus;

    @Column(name = "queued_at", nullable = false)
    private Instant queuedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by", length = 200)
    private String reviewedBy;

    @Column(name = "review_notes", length = 2000)
    private String reviewNotes;

    protected FraudReview() {
    }

    public Long getFraudReviewId() {
        return fraudReviewId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public FraudReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public Instant getQueuedAt() {
        return queuedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }
}
