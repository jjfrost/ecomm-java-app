package com.ecommerce.reporting.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Immutable
@Table(name = "customer_lifetime_value_v", schema = "ecomm")
public class CustomerLifetimeValueReport {
    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "email")
    private String email;

    @Column(name = "order_count")
    private Long orderCount;

    @Column(name = "total_spent")
    private BigDecimal totalSpent;

    @Column(name = "average_order_value")
    private BigDecimal averageOrderValue;

    @Column(name = "first_order_at")
    private Instant firstOrderAt;

    @Column(name = "last_order_at")
    private Instant lastOrderAt;

    @Column(name = "lifetime_value_tier")
    private String lifetimeValueTier;

    protected CustomerLifetimeValueReport() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getEmail() {
        return email;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public Instant getFirstOrderAt() {
        return firstOrderAt;
    }

    public Instant getLastOrderAt() {
        return lastOrderAt;
    }

    public String getLifetimeValueTier() {
        return lifetimeValueTier;
    }
}
