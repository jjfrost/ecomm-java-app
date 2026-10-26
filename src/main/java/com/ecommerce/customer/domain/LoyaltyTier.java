package com.ecommerce.customer.domain;

import java.util.Locale;

public enum LoyaltyTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM;

    public static LoyaltyTier fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The loyalty package returned an empty tier");
        }
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
