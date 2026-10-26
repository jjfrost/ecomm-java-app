package com.ecommerce.customer.api.dto;

import com.ecommerce.customer.domain.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerAddressRequest(
        @NotNull AddressType type,
        @NotBlank @Size(max = 80) String label,
        @NotBlank @Size(max = 250) String street,
        @NotBlank @Size(max = 120) String city,
        @Size(max = 120) String state,
        @NotBlank @Size(max = 20) String zip,
        @NotBlank @Pattern(regexp = "(?i)[a-z]{2}") String countryCode,
        boolean defaultAddress) {
}
