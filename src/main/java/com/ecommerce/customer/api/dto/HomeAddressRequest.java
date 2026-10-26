package com.ecommerce.customer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HomeAddressRequest(
        @NotBlank @Size(max = 250) String street,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 20) String zip) {
}
