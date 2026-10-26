package com.ecommerce.inventory.domain;

import com.ecommerce.common.exception.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "inventory", schema = "ecomm")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_generator")
    @SequenceGenerator(
            name = "inventory_generator",
            sequenceName = "ecomm.inventory_seq",
            allocationSize = 1)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity_on_hand", nullable = false)
    private BigDecimal quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private BigDecimal quantityReserved;

    @Column(name = "reorder_level", nullable = false)
    private BigDecimal reorderLevel;

    @Column(name = "maximum_stock", nullable = false)
    private BigDecimal maximumStock;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Inventory() {
    }

    public BigDecimal availableQuantity() {
        return quantityOnHand.subtract(quantityReserved);
    }

    public boolean requiresReorder() {
        return availableQuantity().compareTo(reorderLevel) <= 0;
    }

    public void assertCanReceive(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessRuleException(
                    "INVALID_RECEIPT_QUANTITY",
                    "Receipt quantity must be greater than zero");
        }
        if (quantityOnHand.add(quantity).compareTo(maximumStock) > 0) {
            throw new BusinessRuleException(
                    "INVENTORY_MAXIMUM_EXCEEDED",
                    "Receipt would exceed the configured maximum stock");
        }
    }

    public void assertCanAdjust(BigDecimal quantityDelta) {
        if (quantityDelta == null || quantityDelta.signum() == 0) {
            throw new BusinessRuleException(
                    "ZERO_STOCK_ADJUSTMENT",
                    "Stock adjustment quantity cannot be zero");
        }
        BigDecimal adjusted = quantityOnHand.add(quantityDelta);
        if (adjusted.compareTo(quantityReserved) < 0) {
            throw new BusinessRuleException(
                    "INVENTORY_RESERVED_STOCK_VIOLATION",
                    "Adjustment cannot reduce on-hand stock below reserved stock");
        }
        if (adjusted.compareTo(maximumStock) > 0) {
            throw new BusinessRuleException(
                    "INVENTORY_MAXIMUM_EXCEEDED",
                    "Adjustment would exceed the configured maximum stock");
        }
    }

    public void assertCanReserve(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessRuleException(
                    "INVALID_RESERVATION_QUANTITY",
                    "Reservation quantity must be greater than zero");
        }
        if (quantity.compareTo(availableQuantity()) > 0) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_AVAILABLE_STOCK",
                    "Requested quantity exceeds available stock");
        }
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        lastUpdated = now;
        if (quantityOnHand == null) {
            quantityOnHand = BigDecimal.ZERO;
        }
        if (quantityReserved == null) {
            quantityReserved = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void updateTimestamp() {
        lastUpdated = Instant.now();
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }

    public BigDecimal getQuantityReserved() {
        return quantityReserved;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public BigDecimal getMaximumStock() {
        return maximumStock;
    }

    public String getLocation() {
        return location;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
