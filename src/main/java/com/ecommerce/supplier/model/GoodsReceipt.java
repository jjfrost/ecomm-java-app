package com.ecommerce.supplier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "goods_receipts", schema = "ecomm")
public class GoodsReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goods_receipt_seq")
    @SequenceGenerator(
            name = "goods_receipt_seq",
            sequenceName = "ecomm.goods_receipt_seq",
            allocationSize = 1)
    @Column(name = "goods_receipt_id")
    private Long goodsReceiptId;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "purchase_order_item_id", nullable = false)
    private Long purchaseOrderItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    @Column(name = "rejected_quantity", nullable = false)
    private Integer rejectedQuantity;

    @Column(name = "receipt_reference", nullable = false, length = 100)
    private String receiptReference;

    @Column(name = "received_by", nullable = false, length = 200)
    private String receivedBy;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    protected GoodsReceipt() {
    }

    public Long getGoodsReceiptId() {
        return goodsReceiptId;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public Long getPurchaseOrderItemId() {
        return purchaseOrderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public Integer getRejectedQuantity() {
        return rejectedQuantity;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public String getNotes() {
        return notes;
    }
}
