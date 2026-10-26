package com.ecommerce.cart.domain;

import com.ecommerce.common.exception.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "shopping_carts", schema = "ecomm")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_cart_generator")
    @SequenceGenerator(
            name = "shopping_cart_generator",
            sequenceName = "ecomm.shopping_cart_seq",
            allocationSize = 1)
    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CartStatus status;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ShoppingCart() {
    }

    public static ShoppingCart create(Long customerId, Instant expiresAt) {
        ShoppingCart cart = new ShoppingCart();
        cart.customerId = customerId;
        cart.expiresAt = expiresAt;
        cart.status = CartStatus.ACTIVE;
        return cart;
    }

    public void assertMutable(Instant now) {
        if (status != CartStatus.ACTIVE) {
            throw new BusinessRuleException("CART_NOT_ACTIVE", "Only an active cart can be changed");
        }
        if (!expiresAt.isAfter(now)) {
            throw new BusinessRuleException("CART_EXPIRED", "The shopping cart has expired");
        }
    }

    public void applyCoupon(String couponCode) {
        this.couponCode = couponCode;
    }

    public void removeCoupon() {
        couponCode = null;
    }

    public void markCheckedOut() {
        assertMutable(Instant.now());
        status = CartStatus.CHECKED_OUT;
    }

    public void abandon() {
        assertMutable(Instant.now());
        status = CartStatus.ABANDONED;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() { updatedAt = Instant.now(); }

    public Long getCartId() { return cartId; }
    public Long getCustomerId() { return customerId; }
    public CartStatus getStatus() { return status; }
    public String getCouponCode() { return couponCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
