package com.ecommerce.catalog.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductBundleItemRequest(
        @NotNull @Positive Long productId,
        @Positive @Max(99) int quantity) {
}
