package com.ecommerce.customer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerPreferenceRequest(
        @NotBlank @Size(max = 500) String value) {
}
