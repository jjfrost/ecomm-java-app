package com.ecommerce.pricing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceListItemResponse(
        Long priceListItemId,
        Long priceListId,
        Long productId,
        BigDecimal price,
        BigDecimal minimumQuantity,
        Instant validFrom,
        Instant validTo,
        Instant createdAt,
        Instant updatedAt) {
}
