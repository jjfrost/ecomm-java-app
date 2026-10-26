package com.ecommerce.ui.form;

import com.ecommerce.catalog.api.dto.ProductDetailResponse;
import com.ecommerce.catalog.api.dto.ProductUpdateRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductEditForm {
    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    private BigDecimal price;

    @NotNull
    @Positive
    private Long categoryId;

    public static ProductEditForm from(ProductDetailResponse product) {
        ProductEditForm form = new ProductEditForm();
        form.name = product.name();
        form.description = product.description();
        form.price = product.price();
        form.categoryId = product.categoryId();
        return form;
    }

    public ProductUpdateRequest toRequest() {
        return new ProductUpdateRequest(name, description, price, categoryId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
