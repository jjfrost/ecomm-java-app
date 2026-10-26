package com.ecommerce.customer.api.dto;

import java.time.Instant;

public record CustomerPreferenceResponse(
        String key,
        String value,
        Instant updatedAt) {
}
