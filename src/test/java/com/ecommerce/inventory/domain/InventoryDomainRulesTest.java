package com.ecommerce.inventory.domain;

import com.ecommerce.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryDomainRulesTest {
    @Test
    void availabilityAndValidStockChangesRespectConfiguredBounds() {
        Inventory inventory = inventory("100.00", "20.00", "80.00", "150.00");

        assertAll(
                () -> assertEquals(new BigDecimal("80.00"), inventory.availableQuantity()),
                () -> assertTrue(inventory.requiresReorder()),
                () -> assertDoesNotThrow(() -> inventory.assertCanReceive(new BigDecimal("50.00"))),
                () -> assertDoesNotThrow(() -> inventory.assertCanAdjust(new BigDecimal("-80.00"))),
                () -> assertDoesNotThrow(() -> inventory.assertCanReserve(new BigDecimal("80.00"))));
    }

    @Test
    void stockChangesRejectMaximumReservedAndAvailabilityViolations() {
        Inventory inventory = inventory("100.00", "20.00", "40.00", "150.00");

        BusinessRuleException receipt = assertThrows(
                BusinessRuleException.class,
                () -> inventory.assertCanReceive(new BigDecimal("50.01")));
        BusinessRuleException adjustment = assertThrows(
                BusinessRuleException.class,
                () -> inventory.assertCanAdjust(new BigDecimal("-80.01")));
        BusinessRuleException reservation = assertThrows(
                BusinessRuleException.class,
                () -> inventory.assertCanReserve(new BigDecimal("80.01")));

        assertAll(
                () -> assertEquals("INVENTORY_MAXIMUM_EXCEEDED", receipt.getRuleCode()),
                () -> assertEquals("INVENTORY_RESERVED_STOCK_VIOLATION", adjustment.getRuleCode()),
                () -> assertEquals("INSUFFICIENT_AVAILABLE_STOCK", reservation.getRuleCode()));
    }

    private static Inventory inventory(
            String onHand,
            String reserved,
            String reorderLevel,
            String maximumStock) {
        Inventory inventory = new Inventory();
        ReflectionTestUtils.setField(inventory, "quantityOnHand", new BigDecimal(onHand));
        ReflectionTestUtils.setField(inventory, "quantityReserved", new BigDecimal(reserved));
        ReflectionTestUtils.setField(inventory, "reorderLevel", new BigDecimal(reorderLevel));
        ReflectionTestUtils.setField(inventory, "maximumStock", new BigDecimal(maximumStock));
        return inventory;
    }
}
