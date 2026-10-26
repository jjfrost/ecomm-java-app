package com.ecommerce.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AuditDtos {
    private AuditDtos() {
    }

    public record WriteAuditEventRequest(
            @NotBlank @Size(max = 80) String eventType,
            @NotBlank @Size(max = 80) String entityType,
            @NotBlank @Size(max = 100) String entityId,
            @NotBlank @Size(max = 200) String actor,
            @Size(max = 100) String correlationId,
            @Size(max = 4000) String eventPayload,
            @Size(max = 64) String sourceIp) {
    }

    public record AuditEventResponse(
            Long auditEventId,
            String eventType,
            String entityType,
            String entityId,
            String actor,
            String correlationId,
            String eventPayload,
            String sourceIp,
            Instant occurredAt) {
    }
}
