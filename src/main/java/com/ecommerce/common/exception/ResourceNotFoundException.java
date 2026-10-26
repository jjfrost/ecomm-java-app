package com.ecommerce.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(resourceType + " was not found for identifier " + identifier);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
