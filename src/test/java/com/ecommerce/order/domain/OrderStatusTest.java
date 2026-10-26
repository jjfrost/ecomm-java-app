package com.ecommerce.order.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {
    @Test
    void transitionMatrixAllowsOnlyForwardFulfillmentAndPreShipmentCancellation() {
        assertAll(
                () -> assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED)),
                () -> assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)),
                () -> assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.SHIPPED)),
                () -> assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PROCESSING)),
                () -> assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED)),
                () -> assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED)),
                () -> assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED)),
                () -> assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED)),
                () -> assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PENDING)));
    }
}
