package com.ecommerce.catalog.api.dto;

import java.math.BigDecimal;

public record ProductBundleItemResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {
}
