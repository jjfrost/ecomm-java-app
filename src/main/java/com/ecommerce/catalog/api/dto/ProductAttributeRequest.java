package com.ecommerce.catalog.api.dto;

import com.ecommerce.catalog.domain.AttributeDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductAttributeRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull AttributeDataType dataType,
        boolean required) {
}
