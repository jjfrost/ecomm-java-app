package com.ecommerce.order.dto;

import com.ecommerce.order.domain.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatusTransitionRequest(
        @NotNull OrderStatus status,
        @NotBlank @Size(max = 100) String changedBy,
        @Size(max = 500) String notes) {
}
