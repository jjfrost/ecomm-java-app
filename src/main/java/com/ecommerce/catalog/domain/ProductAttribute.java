package com.ecommerce.catalog.domain;

import com.ecommerce.common.persistence.OracleYesNoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attributes", schema = "ecomm")
public class ProductAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_attribute_seq_generator")
    @SequenceGenerator(
            name = "product_attribute_seq_generator",
            sequenceName = "product_attribute_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "attribute_id")
    private Long id;

    @Column(name = "attribute_name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private AttributeDataType dataType;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_required", nullable = false, length = 1)
    private Boolean required;

    protected ProductAttribute() {
    }

    public ProductAttribute(String name, AttributeDataType dataType, boolean required) {
        update(name, dataType, required);
    }

    public void update(String name, AttributeDataType dataType, boolean required) {
        this.name = name.trim();
        this.dataType = dataType;
        this.required = required;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AttributeDataType getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return Boolean.TRUE.equals(required);
    }
}
