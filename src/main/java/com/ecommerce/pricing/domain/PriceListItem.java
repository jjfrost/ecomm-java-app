package com.ecommerce.pricing.domain;

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
@Table(name = "price_list_items", schema = "ecomm")
public class PriceListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_list_item_generator")
    @SequenceGenerator(
            name = "price_list_item_generator",
            sequenceName = "ecomm.price_list_item_seq",
            allocationSize = 1)
    @Column(name = "price_list_item_id")
    private Long priceListItemId;

    @Column(name = "price_list_id", nullable = false)
    private Long priceListId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "minimum_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumQuantity;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PriceListItem() {
    }

    public static PriceListItem create(
            Long priceListId,
            Long productId,
            BigDecimal price,
            BigDecimal minimumQuantity,
            Instant validFrom,
            Instant validTo) {
        PriceListItem item = new PriceListItem();
        item.priceListId = priceListId;
        item.applyChanges(productId, price, minimumQuantity, validFrom, validTo);
        return item;
    }

    public void applyChanges(
            Long productId,
            BigDecimal price,
            BigDecimal minimumQuantity,
            Instant validFrom,
            Instant validTo) {
        this.productId = productId;
        this.price = price;
        this.minimumQuantity = minimumQuantity;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getPriceListItemId() { return priceListItemId; }
    public Long getPriceListId() { return priceListId; }
    public Long getProductId() { return productId; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getValidTo() { return validTo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
