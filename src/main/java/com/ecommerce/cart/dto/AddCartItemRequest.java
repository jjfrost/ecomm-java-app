package com.ecommerce.cart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddCartItemRequest(
        @NotNull @Positive Long productId,
        @NotNull @DecimalMin("0.01") BigDecimal quantity) {
}
