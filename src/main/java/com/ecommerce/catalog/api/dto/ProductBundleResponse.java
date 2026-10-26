package com.ecommerce.catalog.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductBundleResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal componentTotal,
        BigDecimal savings,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        List<ProductBundleItemResponse> items) {
}
