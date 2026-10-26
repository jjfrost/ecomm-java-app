package com.ecommerce.customer.api.dto;

import java.time.Instant;
import java.util.List;

public record CustomerDetailResponse(
        Long id,
        String name,
        String email,
        HomeAddressResponse homeAddress,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        List<CustomerAddressResponse> addresses,
        List<CustomerPreferenceResponse> preferences) {
}
