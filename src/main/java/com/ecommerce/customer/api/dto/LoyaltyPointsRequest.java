package com.ecommerce.customer.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LoyaltyPointsRequest(
        @Positive @Max(1_000_000) long points,
        @NotBlank @Size(max = 100) String externalReference,
        @Size(max = 500) String description) {
}
