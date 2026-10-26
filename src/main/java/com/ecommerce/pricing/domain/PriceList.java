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
@Table(name = "price_lists", schema = "ecomm")
public class PriceList {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_list_generator")
    @SequenceGenerator(name = "price_list_generator", sequenceName = "ecomm.price_list_seq", allocationSize = 1)
    @Column(name = "price_list_id")
    private Long priceListId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean active;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PriceList() {
    }

    public static PriceList create(
            String name,
            String currencyCode,
            Instant validFrom,
            Instant validTo,
            boolean active,
            int priority) {
        PriceList priceList = new PriceList();
        priceList.applyChanges(name, currencyCode, validFrom, validTo, active, priority);
        return priceList;
    }

    public void applyChanges(
            String name,
            String currencyCode,
            Instant validFrom,
            Instant validTo,
            boolean active,
            int priority) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = active;
        this.priority = priority;
    }

    public boolean isEffectiveAt(Instant instant) {
        return Boolean.TRUE.equals(active)
                && !validFrom.isAfter(instant)
                && (validTo == null || validTo.isAfter(instant));
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getPriceListId() { return priceListId; }
    public String getName() { return name; }
    public String getCurrencyCode() { return currencyCode; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getValidTo() { return validTo; }
    public Boolean getActive() { return active; }
    public Integer getPriority() { return priority; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
