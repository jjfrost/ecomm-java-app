package com.ecommerce.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductAttributeValueRequest(
        @NotBlank @Size(max = 1000) String value) {
}
