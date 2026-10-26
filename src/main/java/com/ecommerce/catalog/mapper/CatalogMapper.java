package com.ecommerce.catalog.mapper;

import com.ecommerce.catalog.api.dto.CategoryResponse;
import com.ecommerce.catalog.api.dto.ProductAttributeResponse;
import com.ecommerce.catalog.api.dto.ProductAttributeValueResponse;
import com.ecommerce.catalog.api.dto.ProductDetailResponse;
import com.ecommerce.catalog.api.dto.ProductImageResponse;
import com.ecommerce.catalog.api.dto.ProductSummaryResponse;
import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.ProductAttribute;
import com.ecommerce.catalog.domain.ProductAttributeValue;
import com.ecommerce.catalog.domain.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogMapper {
    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }

    public ProductImageResponse toResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getAltText(),
                image.getSortOrder(),
                image.isPrimaryImage());
    }

    public ProductAttributeResponse toResponse(ProductAttribute attribute) {
        return new ProductAttributeResponse(
                attribute.getId(),
                attribute.getName(),
                attribute.getDataType(),
                attribute.isRequired());
    }

    public ProductAttributeValueResponse toResponse(
            ProductAttributeValue value,
            ProductAttribute attribute) {
        return new ProductAttributeValueResponse(
                value.getId(),
                attribute.getId(),
                attribute.getName(),
                attribute.getDataType(),
                attribute.isRequired(),
                value.getValue());
    }

    public ProductSummaryResponse toSummary(
            Product product,
            Category category,
            ProductImage primaryImage) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive(),
                category.getId(),
                category.getName(),
                primaryImage == null ? null : primaryImage.getUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    public ProductDetailResponse toDetail(
            Product product,
            Category category,
            List<ProductImage> images,
            List<ProductAttributeValueResponse> attributes) {
        return new ProductDetailResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                category.getId(),
                category.getName(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                images.stream().map(this::toResponse).toList(),
                List.copyOf(attributes));
    }
}
