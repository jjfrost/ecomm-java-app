package com.ecommerce.catalog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogDomainRulesTest {
    @Test
    void productStartsAsDraftNormalizesSkuAndSupportsLifecycle() {
        Product product = new Product(
                " widget-100 ",
                " Widget ",
                " Initial description ",
                new BigDecimal("19.99"),
                3L);

        assertEquals("WIDGET-100", product.getSku());
        assertEquals("Widget", product.getName());
        assertFalse(product.isActive());

        product.activate();
        product.updateDetails("Updated Widget", " ", new BigDecimal("17.50"), 4L);

        assertTrue(product.isActive());
        assertEquals("Updated Widget", product.getName());
        assertNull(product.getDescription());
        assertEquals(new BigDecimal("17.50"), product.getPrice());
        assertEquals(4L, product.getCategoryId());

        product.deactivate();
        assertFalse(product.isActive());
    }
}
