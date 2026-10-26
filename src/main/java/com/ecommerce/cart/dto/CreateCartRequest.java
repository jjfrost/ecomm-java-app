package com.ecommerce.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateCartRequest(@NotNull @Positive Long customerId) {
}
