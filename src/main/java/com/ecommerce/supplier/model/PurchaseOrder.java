package com.ecommerce.supplier.model;

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
import java.time.LocalDate;

@Entity
@Table(name = "purchase_orders", schema = "ecomm")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_order_seq")
    @SequenceGenerator(
            name = "purchase_order_seq",
            sequenceName = "ecomm.purchase_order_seq",
            allocationSize = 1)
    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(name = "po_number", nullable = false, unique = true, length = 50)
    private String poNumber;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_order_status", nullable = false, length = 30)
    private PurchaseOrderStatus purchaseOrderStatus;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_date", nullable = false)
    private LocalDate expectedDate;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_by", nullable = false, length = 200)
    private String createdBy;

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PurchaseOrder() {
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public PurchaseOrderStatus getPurchaseOrderStatus() {
        return purchaseOrderStatus;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
