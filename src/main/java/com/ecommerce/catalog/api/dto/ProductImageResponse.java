package com.ecommerce.catalog.api.dto;

public record ProductImageResponse(
        Long id,
        String url,
        String altText,
        int sortOrder,
        boolean primaryImage) {
}
