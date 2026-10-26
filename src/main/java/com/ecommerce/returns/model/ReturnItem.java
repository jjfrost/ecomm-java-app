package com.ecommerce.returns.model;

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

@Entity
@Table(name = "return_items", schema = "ecomm")
public class ReturnItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "return_item_seq")
    @SequenceGenerator(
            name = "return_item_seq",
            sequenceName = "ecomm.return_item_seq",
            allocationSize = 1)
    @Column(name = "return_item_id")
    private Long returnItemId;

    @Column(name = "return_request_id", nullable = false)
    private Long returnRequestId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "item_condition", length = 200)
    private String itemCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", nullable = false, length = 30)
    private ReturnDisposition disposition;

    @Column(name = "refund_amount", precision = 19, scale = 2)
    private BigDecimal refundAmount;

    protected ReturnItem() {
    }

    public Long getReturnItemId() {
        return returnItemId;
    }

    public Long getReturnRequestId() {
        return returnRequestId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getItemCondition() {
        return itemCondition;
    }

    public ReturnDisposition getDisposition() {
        return disposition;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
}
