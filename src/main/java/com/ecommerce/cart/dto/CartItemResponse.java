package com.ecommerce.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal unitDiscount,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal total) {
}
