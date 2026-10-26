package com.ecommerce.catalog.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductDetailResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Long categoryId,
        String categoryName,
        Instant createdAt,
        Instant updatedAt,
        List<ProductImageResponse> images,
        List<ProductAttributeValueResponse> attributes) {
}
