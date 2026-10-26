package com.ecommerce.customer.api.dto;

import com.ecommerce.customer.domain.AddressType;

import java.time.Instant;

public record CustomerAddressResponse(
        Long id,
        AddressType type,
        String label,
        String street,
        String city,
        String state,
        String zip,
        String countryCode,
        boolean defaultAddress,
        Instant createdAt,
        Instant updatedAt) {
}
