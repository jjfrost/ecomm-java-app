package com.ecommerce.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "loyalty_accounts", schema = "ecomm")
public class LoyaltyAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loyalty_account_seq_generator")
    @SequenceGenerator(
            name = "loyalty_account_seq_generator",
            sequenceName = "loyalty_account_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "loyalty_account_id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "points_balance", nullable = false)
    private Long pointsBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 20)
    private LoyaltyTier tier;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LoyaltyAccount() {
    }

    public LoyaltyAccount(Long customerId) {
        this.customerId = customerId;
        this.pointsBalance = 0L;
        this.tier = LoyaltyTier.BRONZE;
    }

    public long credit(long points) {
        pointsBalance = Math.addExact(pointsBalance, points);
        return pointsBalance;
    }

    public long redeem(long points) {
        pointsBalance -= points;
        return pointsBalance;
    }

    public void changeTier(LoyaltyTier tier) {
        this.tier = tier;
    }

    @PrePersist
    void initializeAuditFields() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (pointsBalance == null) {
            pointsBalance = 0L;
        }
        if (tier == null) {
            tier = LoyaltyTier.BRONZE;
        }
    }

    @PreUpdate
    void updateAuditFields() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public long getPointsBalance() {
        return pointsBalance;
    }

    public LoyaltyTier getTier() {
        return tier;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
