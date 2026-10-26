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
@Table(name = "product_sales_mv", schema = "ecomm")
public class ProductSalesReport {
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "units_sold")
    private Long unitsSold;

    @Column(name = "order_count")
    private Long orderCount;

    @Column(name = "gross_revenue")
    private BigDecimal grossRevenue;

    @Column(name = "net_revenue")
    private BigDecimal netRevenue;

    @Column(name = "average_unit_price")
    private BigDecimal averageUnitPrice;

    @Column(name = "last_sold_at")
    private Instant lastSoldAt;

    protected ProductSalesReport() {
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getUnitsSold() {
        return unitsSold;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public BigDecimal getAverageUnitPrice() {
        return averageUnitPrice;
    }

    public Instant getLastSoldAt() {
        return lastSoldAt;
    }
}
