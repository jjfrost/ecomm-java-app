package com.ecommerce.order.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.CancelOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.OrderStatusHistoryResponse;
import com.ecommerce.order.dto.OrderStatusTransitionRequest;
import com.ecommerce.order.dto.SubmitOrderRequest;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> submit(
            @Valid @RequestBody SubmitOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.submit(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<OrderResponse> list(
            @RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        return orderService.list(customerId, status, pageable);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable @Positive Long orderId) {
        return orderService.get(orderId);
    }

    @PostMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public OrderResponse transition(
            @PathVariable @Positive Long orderId,
            @Valid @RequestBody OrderStatusTransitionRequest request) {
        return orderService.transition(orderId, request);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(
            @PathVariable @Positive Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        return orderService.cancel(orderId, request);
    }

    @GetMapping("/{orderId}/history")
    public PageResponse<OrderStatusHistoryResponse> history(
            @PathVariable @Positive Long orderId,
            Pageable pageable) {
        return orderService.history(orderId, pageable);
    }
}
