package com.ecommerce.catalog.domain;

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
import java.util.Locale;

@Entity
@Table(name = "products", schema = "ecomm")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_generator")
    @SequenceGenerator(
            name = "product_seq_generator",
            sequenceName = "product_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    protected Product() {
    }

    public Product(
            String sku,
            String name,
            String description,
            BigDecimal price,
            Long categoryId) {
        this.sku = normalizeSku(sku);
        this.active = false;
        updateDetails(name, description, price, categoryId);
    }

    public void updateDetails(
            String name,
            String description,
            BigDecimal price,
            Long categoryId) {
        this.name = name.trim();
        this.description = description == null || description.isBlank()
                ? null
                : description.trim();
        this.price = price;
        this.categoryId = categoryId;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    @PrePersist
    void initializeAuditFields() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) {
            active = false;
        }
    }

    @PreUpdate
    void updateAuditFields() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    private static String normalizeSku(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
