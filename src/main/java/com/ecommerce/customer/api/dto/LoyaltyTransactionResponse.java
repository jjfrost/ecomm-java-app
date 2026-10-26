package com.ecommerce.customer.api.dto;

import com.ecommerce.customer.domain.LoyaltyTransactionType;

import java.time.Instant;

public record LoyaltyTransactionResponse(
        Long id,
        LoyaltyTransactionType type,
        long pointsDelta,
        long balanceAfter,
        String externalReference,
        String description,
        Instant createdAt) {
}
