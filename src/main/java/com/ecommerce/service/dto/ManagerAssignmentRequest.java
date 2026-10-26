package com.ecommerce.service.dto;

import jakarta.validation.constraints.NotNull;

public record ManagerAssignmentRequest(@NotNull Long managerId) {
}
