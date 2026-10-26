package com.ecommerce.catalog.api.dto;

import com.ecommerce.catalog.domain.AttributeDataType;

public record ProductAttributeResponse(
        Long id,
        String name,
        AttributeDataType dataType,
        boolean required) {
}
