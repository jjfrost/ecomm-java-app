package com.ecommerce.order.dto;

import com.ecommerce.order.domain.OrderStatus;

import java.time.Instant;

public record OrderStatusHistoryResponse(
        Long historyId,
        Long orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant changedAt,
        String changedBy,
        String notes) {
}
