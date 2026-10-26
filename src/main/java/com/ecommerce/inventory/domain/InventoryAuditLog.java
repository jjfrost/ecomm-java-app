package com.ecommerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_audit_log", schema = "ecomm")
public class InventoryAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_audit_generator")
    @SequenceGenerator(
            name = "inventory_audit_generator",
            sequenceName = "ecomm.inventory_audit_seq",
            allocationSize = 1)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "operation", nullable = false, length = 20)
    private String operation;

    @Column(name = "old_quantity_on_hand")
    private BigDecimal oldQuantityOnHand;

    @Column(name = "new_quantity_on_hand")
    private BigDecimal newQuantityOnHand;

    @Column(name = "old_quantity_reserved")
    private BigDecimal oldQuantityReserved;

    @Column(name = "new_quantity_reserved")
    private BigDecimal newQuantityReserved;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected InventoryAuditLog() {
    }

    public Long getAuditId() {
        return auditId;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public String getOperation() {
        return operation;
    }

    public BigDecimal getOldQuantityOnHand() {
        return oldQuantityOnHand;
    }

    public BigDecimal getNewQuantityOnHand() {
        return newQuantityOnHand;
    }

    public BigDecimal getOldQuantityReserved() {
        return oldQuantityReserved;
    }

    public BigDecimal getNewQuantityReserved() {
        return newQuantityReserved;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
