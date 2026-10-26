package com.ecommerce.pricing.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EffectivePriceResponse(
        Long productId,
        BigDecimal quantity,
        Long priceListId,
        String currencyCode,
        BigDecimal basePrice,
        BigDecimal effectiveUnitPrice,
        BigDecimal unitDiscount,
        String couponCode,
        Instant pricedAt) {
}
