package com.ecommerce.pricing.dto;

import java.time.Instant;

public record PriceListResponse(
        Long priceListId,
        String name,
        String currencyCode,
        Instant validFrom,
        Instant validTo,
        boolean active,
        Integer priority,
        Instant createdAt,
        Instant updatedAt) {
}
