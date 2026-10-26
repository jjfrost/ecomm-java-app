package com.ecommerce.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "loyalty_transactions", schema = "ecomm")
public class LoyaltyTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loyalty_transaction_seq_generator")
    @SequenceGenerator(
            name = "loyalty_transaction_seq_generator",
            sequenceName = "loyalty_transaction_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "loyalty_transaction_id")
    private Long id;

    @Column(name = "loyalty_account_id", nullable = false)
    private Long loyaltyAccountId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private LoyaltyTransactionType type;

    @Column(name = "points_delta", nullable = false)
    private Long pointsDelta;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "external_reference", nullable = false, length = 100)
    private String externalReference;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LoyaltyTransaction() {
    }

    public LoyaltyTransaction(
            Long loyaltyAccountId,
            Long customerId,
            LoyaltyTransactionType type,
            long pointsDelta,
            long balanceAfter,
            String externalReference,
            String description) {
        this.loyaltyAccountId = loyaltyAccountId;
        this.customerId = customerId;
        this.type = type;
        this.pointsDelta = pointsDelta;
        this.balanceAfter = balanceAfter;
        this.externalReference = externalReference.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
    }

    @PrePersist
    void initializeCreatedAt() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getLoyaltyAccountId() {
        return loyaltyAccountId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LoyaltyTransactionType getType() {
        return type;
    }

    public long getPointsDelta() {
        return pointsDelta;
    }

    public long getBalanceAfter() {
        return balanceAfter;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
