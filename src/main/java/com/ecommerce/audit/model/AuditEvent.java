package com.ecommerce.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_events", schema = "ecomm")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_event_seq")
    @SequenceGenerator(
            name = "audit_event_seq",
            sequenceName = "ecomm.audit_event_seq",
            allocationSize = 1)
    @Column(name = "audit_event_id")
    private Long auditEventId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "entity_type", nullable = false, length = 80)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @Column(name = "actor", nullable = false, length = 200)
    private String actor;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Lob
    @Column(name = "event_payload")
    private String eventPayload;

    @Column(name = "source_ip", length = 64)
    private String sourceIp;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEvent() {
    }

    public AuditEvent(
            String eventType,
            String entityType,
            String entityId,
            String actor,
            String correlationId,
            String eventPayload,
            String sourceIp) {
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actor = actor;
        this.correlationId = correlationId;
        this.eventPayload = eventPayload;
        this.sourceIp = sourceIp;
    }

    @PrePersist
    void initializeOccurredAt() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    public Long getAuditEventId() {
        return auditEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getActor() {
        return actor;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getEventPayload() {
        return eventPayload;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
