package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;

import java.math.BigDecimal;

public interface OrderCommandGateway {
    Long submitOrder(
            Long customerId,
            Long cartId,
            BigDecimal totalAmount,
            String requestedBy);

    void addOrderItem(
            Long orderId,
            Long productId,
            BigDecimal quantity,
            BigDecimal unitPrice);

    void updateStatus(
            Order order,
            OrderStatus status,
            String changedBy,
            String notes);

    void cancelOrder(Order order, String reason, String changedBy);
}
