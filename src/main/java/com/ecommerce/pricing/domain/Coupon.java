package com.ecommerce.pricing.domain;

import com.ecommerce.common.persistence.OracleYesNoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "coupons", schema = "ecomm")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coupon_generator")
    @SequenceGenerator(name = "coupon_generator", sequenceName = "ecomm.coupon_seq", allocationSize = 1)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Column(name = "usage_limit")
    private Long usageLimit;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount;

    @Column(name = "per_customer_limit")
    private Integer perCustomerLimit;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Coupon() {
    }

    public static Coupon create(
            String code,
            Long promotionId,
            Instant validFrom,
            Instant validTo,
            Long usageLimit,
            Integer perCustomerLimit,
            boolean active) {
        Coupon coupon = new Coupon();
        coupon.usageCount = 0L;
        coupon.applyChanges(code, promotionId, validFrom, validTo, usageLimit, perCustomerLimit, active);
        return coupon;
    }

    public void applyChanges(
            String code,
            Long promotionId,
            Instant validFrom,
            Instant validTo,
            Long usageLimit,
            Integer perCustomerLimit,
            boolean active) {
        this.code = code;
        this.promotionId = promotionId;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.usageLimit = usageLimit;
        this.perCustomerLimit = perCustomerLimit;
        this.active = active;
    }

    public boolean isAvailableAt(Instant instant) {
        return Boolean.TRUE.equals(active)
                && !validFrom.isAfter(instant)
                && validTo.isAfter(instant)
                && (usageLimit == null || usageCount < usageLimit);
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() { updatedAt = Instant.now(); }

    public Long getCouponId() { return couponId; }
    public String getCode() { return code; }
    public Long getPromotionId() { return promotionId; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getValidTo() { return validTo; }
    public Long getUsageLimit() { return usageLimit; }
    public Long getUsageCount() { return usageCount; }
    public Integer getPerCustomerLimit() { return perCustomerLimit; }
    public Boolean getActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
