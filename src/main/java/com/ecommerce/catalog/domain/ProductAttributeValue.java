package com.ecommerce.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attribute_values", schema = "ecomm")
public class ProductAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_attribute_value_seq_generator")
    @SequenceGenerator(
            name = "product_attribute_value_seq_generator",
            sequenceName = "product_attribute_value_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "value_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;

    @Column(name = "attribute_value", nullable = false, length = 1000)
    private String value;

    protected ProductAttributeValue() {
    }

    public ProductAttributeValue(Long productId, Long attributeId, String value) {
        this.productId = productId;
        this.attributeId = attributeId;
        this.value = value.trim();
    }

    public void changeValue(String value) {
        this.value = value.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }
}
