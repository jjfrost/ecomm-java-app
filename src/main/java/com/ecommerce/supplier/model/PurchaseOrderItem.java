package com.ecommerce.supplier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items", schema = "ecomm")
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_order_item_seq")
    @SequenceGenerator(
            name = "purchase_order_item_seq",
            sequenceName = "ecomm.purchase_order_item_seq",
            allocationSize = 1)
    @Column(name = "purchase_order_item_id")
    private Long purchaseOrderItemId;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "supplier_product_id", nullable = false)
    private Long supplierProductId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "ordered_quantity", nullable = false)
    private Integer orderedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    protected PurchaseOrderItem() {
    }

    public Long getPurchaseOrderItemId() {
        return purchaseOrderItemId;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public Long getSupplierProductId() {
        return supplierProductId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getOrderedQuantity() {
        return orderedQuantity;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
