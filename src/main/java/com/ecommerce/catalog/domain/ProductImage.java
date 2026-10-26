package com.ecommerce.catalog.domain;

import com.ecommerce.common.persistence.OracleYesNoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_images", schema = "ecomm")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_image_seq_generator")
    @SequenceGenerator(
            name = "product_image_seq_generator",
            sequenceName = "product_image_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String url;

    @Column(name = "alt_text", nullable = false, length = 250)
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_primary", nullable = false, length = 1)
    private Boolean primaryImage;

    protected ProductImage() {
    }

    public ProductImage(
            Long productId,
            String url,
            String altText,
            int sortOrder,
            boolean primaryImage) {
        this.productId = productId;
        update(url, altText, sortOrder);
        this.primaryImage = primaryImage;
    }

    public void update(String url, String altText, int sortOrder) {
        this.url = url.trim();
        this.altText = altText.trim();
        this.sortOrder = sortOrder;
    }

    public void makePrimary() {
        primaryImage = true;
    }

    public void clearPrimary() {
        primaryImage = false;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getUrl() {
        return url;
    }

    public String getAltText() {
        return altText;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isPrimaryImage() {
        return Boolean.TRUE.equals(primaryImage);
    }
}
