package com.ecommerce.customer.api.dto;

import com.ecommerce.customer.domain.LoyaltyTier;

import java.time.Instant;

public record LoyaltyAccountResponse(
        Long id,
        Long customerId,
        long pointsBalance,
        LoyaltyTier tier,
        Instant createdAt,
        Instant updatedAt) {
}
