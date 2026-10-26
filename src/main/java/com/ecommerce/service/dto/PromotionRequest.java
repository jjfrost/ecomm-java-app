package com.ecommerce.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record PromotionRequest(
        @DecimalMin("0.01") @DecimalMax("100.00") double raisePercentage) {
}
