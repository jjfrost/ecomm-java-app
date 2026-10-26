package com.ecommerce.common.api;

public record FieldViolation(String field, String message, Object rejectedValue) {
}
