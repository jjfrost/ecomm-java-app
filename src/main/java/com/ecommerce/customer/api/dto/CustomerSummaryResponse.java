package com.ecommerce.customer.api.dto;

import java.time.Instant;

public record CustomerSummaryResponse(
        Long id,
        String name,
        String email,
        HomeAddressResponse homeAddress,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
