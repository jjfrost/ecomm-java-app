package com.ecommerce.cart.dto;

import com.ecommerce.cart.domain.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResponse(
        Long cartId,
        Long customerId,
        CartStatus status,
        String couponCode,
        List<CartItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal total,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt) {
}
