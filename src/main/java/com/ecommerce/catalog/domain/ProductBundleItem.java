package com.ecommerce.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_bundle_items", schema = "ecomm")
public class ProductBundleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_bundle_item_seq_generator")
    @SequenceGenerator(
            name = "product_bundle_item_seq_generator",
            sequenceName = "product_bundle_item_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "bundle_item_id")
    private Long id;

    @Column(name = "bundle_id", nullable = false)
    private Long bundleId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    protected ProductBundleItem() {
    }

    public ProductBundleItem(Long bundleId, Long productId, int quantity) {
        this.bundleId = bundleId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getBundleId() {
        return bundleId;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
