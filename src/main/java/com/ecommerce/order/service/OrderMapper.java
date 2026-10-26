package com.ecommerce.order.service;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderItem;
import com.ecommerce.order.domain.OrderStatusHistory;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.OrderStatusHistoryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {
    public OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getOrderItemId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.lineTotal());
    }

    public OrderResponse toResponse(Order order, List<OrderItem> items) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                items.stream().map(this::toResponse).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    public OrderStatusHistoryResponse toResponse(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getHistoryId(),
                history.getOrderId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedAt(),
                history.getChangedBy(),
                history.getNotes());
    }
}
