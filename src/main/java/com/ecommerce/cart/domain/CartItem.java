package com.ecommerce.cart.domain;

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
@Table(name = "cart_items", schema = "ecomm")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_item_generator")
    @SequenceGenerator(name = "cart_item_generator", sequenceName = "ecomm.cart_item_seq", allocationSize = 1)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "unit_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitDiscount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CartItem() {
    }

    public static CartItem create(
            Long cartId,
            Long productId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal unitDiscount) {
        CartItem item = new CartItem();
        item.cartId = cartId;
        item.productId = productId;
        item.reprice(quantity, unitPrice, unitDiscount);
        return item;
    }

    public void reprice(BigDecimal quantity, BigDecimal unitPrice, BigDecimal unitDiscount) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitDiscount = unitDiscount;
    }

    public BigDecimal subtotal() { return unitPrice.multiply(quantity); }
    public BigDecimal discountTotal() { return unitDiscount.multiply(quantity); }
    public BigDecimal total() { return subtotal().subtract(discountTotal()).max(BigDecimal.ZERO); }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() { updatedAt = Instant.now(); }

    public Long getCartItemId() { return cartItemId; }
    public Long getCartId() { return cartId; }
    public Long getProductId() { return productId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getUnitDiscount() { return unitDiscount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
