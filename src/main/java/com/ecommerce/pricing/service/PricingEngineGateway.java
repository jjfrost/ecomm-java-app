package com.ecommerce.pricing.service;

import java.math.BigDecimal;
import java.time.Instant;

public interface PricingEngineGateway {
    BigDecimal getEffectivePrice(
            Long productId,
            BigDecimal quantity,
            Long customerId,
            String couponCode,
            Instant asOf);

    boolean validateCoupon(
            String couponCode,
            Long customerId,
            BigDecimal subtotal,
            Instant asOf);
}
