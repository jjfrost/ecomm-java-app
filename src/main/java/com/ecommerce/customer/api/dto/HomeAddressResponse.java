package com.ecommerce.customer.api.dto;

public record HomeAddressResponse(
        String street,
        String city,
        String zip) {
}
