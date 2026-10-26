package com.ecommerce.catalog.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductBundleRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal price,
        @NotNull @Size(min = 2, max = 25) List<@Valid ProductBundleItemRequest> items) {
}
