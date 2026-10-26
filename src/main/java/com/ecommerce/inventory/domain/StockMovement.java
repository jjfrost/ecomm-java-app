package com.ecommerce.inventory.domain;

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
@Table(name = "stock_movements", schema = "ecomm")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_movement_generator")
    @SequenceGenerator(
            name = "stock_movement_generator",
            sequenceName = "ecomm.stock_movement_seq",
            allocationSize = 1)
    @Column(name = "movement_id")
    private Long movementId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private StockMovementType movementType;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StockMovement() {
    }

    public Long getMovementId() {
        return movementId;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getReason() {
        return reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
