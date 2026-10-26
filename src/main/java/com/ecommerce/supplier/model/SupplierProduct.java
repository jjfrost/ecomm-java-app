package com.ecommerce.supplier.model;

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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "supplier_products", schema = "ecomm")
public class SupplierProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supplier_product_seq")
    @SequenceGenerator(
            name = "supplier_product_seq",
            sequenceName = "ecomm.supplier_product_seq",
            allocationSize = 1)
    @Column(name = "supplier_product_id")
    private Long supplierProductId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "supplier_sku", nullable = false, length = 100)
    private String supplierSku;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "minimum_order_quantity", nullable = false)
    private Integer minimumOrderQuantity;

    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "active_flag", nullable = false, length = 1)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SupplierProduct() {
    }

    public SupplierProduct(
            Long supplierId,
            Long productId,
            String supplierSku,
            BigDecimal unitCost,
            Integer minimumOrderQuantity,
            Integer leadTimeDays) {
        this.supplierId = supplierId;
        this.productId = productId;
        this.supplierSku = supplierSku;
        this.unitCost = unitCost;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.leadTimeDays = leadTimeDays;
        this.active = true;
    }

    public void update(
            String supplierSku,
            BigDecimal unitCost,
            Integer minimumOrderQuantity,
            Integer leadTimeDays,
            Boolean active) {
        this.supplierSku = supplierSku;
        this.unitCost = unitCost;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.leadTimeDays = leadTimeDays;
        this.active = active;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getSupplierProductId() {
        return supplierProductId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSupplierSku() {
        return supplierSku;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public Integer getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public Boolean getActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
