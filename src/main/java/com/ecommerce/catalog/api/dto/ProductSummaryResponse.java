package com.ecommerce.catalog.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductSummaryResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        boolean active,
        Long categoryId,
        String categoryName,
        String primaryImageUrl,
        Instant createdAt,
        Instant updatedAt) {
}
