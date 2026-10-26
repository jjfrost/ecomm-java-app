package com.ecommerce.catalog.api.dto;

import com.ecommerce.catalog.domain.AttributeDataType;

public record ProductAttributeValueResponse(
        Long id,
        Long attributeId,
        String attributeName,
        AttributeDataType dataType,
        boolean required,
        String value) {
}
