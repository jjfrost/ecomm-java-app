package com.ecommerce.customer.gateway;

import com.ecommerce.customer.domain.LoyaltyTier;

public interface LoyaltyTierGateway {
    LoyaltyTier calculateTier(long pointsBalance);
}
