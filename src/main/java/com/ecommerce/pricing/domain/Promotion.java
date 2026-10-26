package com.ecommerce.pricing.domain;

import com.ecommerce.common.persistence.OracleYesNoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "promotions", schema = "ecomm")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "promotion_generator")
    @SequenceGenerator(name = "promotion_generator", sequenceName = "ecomm.promotion_seq", allocationSize = 1)
    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 30)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean active;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "usage_limit")
    private Long usageLimit;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Promotion() {
    }

    public static Promotion create(
            String name,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            Instant validFrom,
            Instant validTo,
            boolean active,
            int priority,
            Long usageLimit) {
        Promotion promotion = new Promotion();
        promotion.usageCount = 0L;
        promotion.applyChanges(
                name, description, discountType, discountValue,
                validFrom, validTo, active, priority, usageLimit);
        return promotion;
    }

    public void applyChanges(
            String name,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            Instant validFrom,
            Instant validTo,
            boolean active,
            int priority,
            Long usageLimit) {
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = active;
        this.priority = priority;
        this.usageLimit = usageLimit;
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

    public Long getPromotionId() { return promotionId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getValidTo() { return validTo; }
    public Boolean getActive() { return active; }
    public Integer getPriority() { return priority; }
    public Long getUsageLimit() { return usageLimit; }
    public Long getUsageCount() { return usageCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
