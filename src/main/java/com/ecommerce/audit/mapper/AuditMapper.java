package com.ecommerce.audit.mapper;

import com.ecommerce.audit.dto.AuditDtos.AuditEventResponse;
import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.model.AuditEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {
    public AuditEvent toEntity(WriteAuditEventRequest request) {
        return new AuditEvent(
                request.eventType(),
                request.entityType(),
                request.entityId(),
                request.actor(),
                request.correlationId(),
                request.eventPayload(),
                request.sourceIp());
    }

    public AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(
                event.getAuditEventId(),
                event.getEventType(),
                event.getEntityType(),
                event.getEntityId(),
                event.getActor(),
                event.getCorrelationId(),
                event.getEventPayload(),
                event.getSourceIp(),
                event.getOccurredAt());
    }
}
