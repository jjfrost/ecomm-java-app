package com.ecommerce.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductImageRequest(
        @NotBlank
        @Size(max = 1000)
        String url,
        @NotBlank @Size(max = 250) String altText,
        @PositiveOrZero int sortOrder,
        boolean primaryImage) {
}
