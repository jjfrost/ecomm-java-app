package com.ecommerce.reporting.dto;

import java.math.BigDecimal;
import java.time.Instant;

public final class ReportingDtos {
    private ReportingDtos() {
    }

    public record CustomerLifetimeValueResponse(
            Long customerId,
            String customerName,
            String email,
            Long orderCount,
            BigDecimal totalSpent,
            BigDecimal averageOrderValue,
            Instant firstOrderAt,
            Instant lastOrderAt,
            String lifetimeValueTier) {
    }

    public record ProductSalesResponse(
            Long productId,
            String productName,
            Long categoryId,
            Long unitsSold,
            Long orderCount,
            BigDecimal grossRevenue,
            BigDecimal netRevenue,
            BigDecimal averageUnitPrice,
            Instant lastSoldAt) {
    }
}
