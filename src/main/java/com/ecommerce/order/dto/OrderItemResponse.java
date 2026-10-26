package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long orderItemId,
        Long productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
