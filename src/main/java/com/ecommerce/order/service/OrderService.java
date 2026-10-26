package com.ecommerce.order.service;

import com.ecommerce.cart.dto.CartItemResponse;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.CancelOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.OrderStatusHistoryResponse;
import com.ecommerce.order.dto.OrderStatusTransitionRequest;
import com.ecommerce.order.dto.SubmitOrderRequest;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OrderStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Service
public class OrderService {
    private static final Duration RESERVATION_LIFETIME = Duration.ofHours(2);

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final OrderCommandGateway commandGateway;
    private final OrderMapper mapper;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository itemRepository,
            OrderStatusHistoryRepository historyRepository,
            CartService cartService,
            InventoryService inventoryService,
            OrderCommandGateway commandGateway,
            OrderMapper mapper) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.historyRepository = historyRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.commandGateway = commandGateway;
        this.mapper = mapper;
    }

    @Transactional
    public OrderResponse submit(SubmitOrderRequest request) {
        CartResponse cart = cartService.checkoutSnapshot(request.cartId());
        BigDecimal itemTotal = cart.items().stream()
                .map(CartItemResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (itemTotal.compareTo(cart.total()) != 0) {
            throw new BusinessRuleException(
                    "CART_TOTAL_MISMATCH",
                    "Cart total does not equal the sum of its lines");
        }
        String requestedBy = request.requestedBy().trim();
        Long orderId = commandGateway.submitOrder(
                cart.customerId(),
                cart.cartId(),
                cart.total(),
                requestedBy);
        if (orderId == null) {
            throw new BusinessRuleException(
                    "ORDER_ID_NOT_RETURNED",
                    "Order package did not return an order identifier");
        }
        Instant reservationExpiry = Instant.now().plus(RESERVATION_LIFETIME);
        for (CartItemResponse item : cart.items()) {
            BigDecimal effectiveUnitPrice = item.unitPrice().subtract(item.unitDiscount());
            inventoryService.reserveProduct(
                    item.productId(),
                    orderId,
                    item.quantity(),
                    reservationExpiry,
                    requestedBy);
            commandGateway.addOrderItem(
                    orderId,
                    item.productId(),
                    item.quantity(),
                    effectiveUnitPrice);
        }
        cartService.markCheckedOut(cart.cartId());
        Order order = requireOrder(orderId);
        return currentResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long orderId) {
        return currentResponse(requireOrder(orderId));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> list(
            Long customerId,
            OrderStatus status,
            Pageable pageable) {
        Page<Order> page;
        if (customerId != null && status != null) {
            page = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else if (customerId != null) {
            page = orderRepository.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            page = orderRepository.findByStatus(status, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return PageResponse.from(page, this::currentResponse);
    }

    @Transactional
    public OrderResponse transition(
            Long orderId,
            OrderStatusTransitionRequest request) {
        Order order = requireLockedOrder(orderId);
        if (request.status() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "USE_ORDER_CANCELLATION",
                    "Use the cancellation operation to cancel an order");
        }
        assertTransition(order, request.status());
        commandGateway.updateStatus(
                order,
                request.status(),
                request.changedBy().trim(),
                trimToNull(request.notes()));
        return currentResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long orderId, CancelOrderRequest request) {
        Order order = requireLockedOrder(orderId);
        assertTransition(order, OrderStatus.CANCELLED);
        String changedBy = request.changedBy().trim();
        String reason = request.reason().trim();
        commandGateway.cancelOrder(order, reason, changedBy);
        inventoryService.releaseForOrder(orderId, reason, changedBy);
        return currentResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderStatusHistoryResponse> history(
            Long orderId,
            Pageable pageable) {
        requireOrder(orderId);
        return PageResponse.from(
                historyRepository.findByOrderIdOrderByChangedAtDesc(orderId, pageable),
                mapper::toResponse);
    }

    private void assertTransition(Order order, OrderStatus target) {
        if (!order.getStatus().canTransitionTo(target)) {
            throw new BusinessRuleException(
                    "INVALID_ORDER_STATUS_TRANSITION",
                    "Order cannot transition from " + order.getStatus() + " to " + target);
        }
    }

    private OrderResponse currentResponse(Order order) {
        return mapper.toResponse(
                order,
                itemRepository.findByOrderIdOrderByOrderItemId(order.getOrderId()));
    }

    private Order requireOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    private Order requireLockedOrder(Long orderId) {
        return orderRepository.findLockedById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
