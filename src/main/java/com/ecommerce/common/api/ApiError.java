package com.ecommerce.common.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        List<FieldViolation> violations) {

    public static ApiError of(
            int status,
            String error,
            String message,
            String path,
            String correlationId) {
        return new ApiError(
                Instant.now(),
                status,
                error,
                message,
                path,
                correlationId,
                List.of());
    }

    public static ApiError withViolations(
            int status,
            String error,
            String message,
            String path,
            String correlationId,
            List<FieldViolation> violations) {
        return new ApiError(
                Instant.now(),
                status,
                error,
                message,
                path,
                correlationId,
                List.copyOf(violations));
    }
}
