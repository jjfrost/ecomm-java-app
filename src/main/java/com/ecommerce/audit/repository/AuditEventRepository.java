package com.ecommerce.audit.repository;

import com.ecommerce.audit.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    @Query("""
            SELECT event
              FROM AuditEvent event
             WHERE (:eventType IS NULL OR UPPER(event.eventType) = UPPER(:eventType))
               AND (:entityType IS NULL OR UPPER(event.entityType) = UPPER(:entityType))
               AND (:entityId IS NULL OR event.entityId = :entityId)
               AND (:actor IS NULL OR UPPER(event.actor) = UPPER(:actor))
               AND (:correlationId IS NULL OR event.correlationId = :correlationId)
               AND (:fromTime IS NULL OR event.occurredAt >= :fromTime)
               AND (:toTime IS NULL OR event.occurredAt < :toTime)
            """)
    Page<AuditEvent> search(
            @Param("eventType") String eventType,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            @Param("actor") String actor,
            @Param("correlationId") String correlationId,
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime,
            Pageable pageable);
}
