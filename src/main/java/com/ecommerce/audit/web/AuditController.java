package com.ecommerce.audit.web;

import com.ecommerce.audit.dto.AuditDtos.AuditEventResponse;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.api.PageResponse;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/audit-events")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/{auditEventId}")
    public AuditEventResponse getById(@PathVariable @Positive Long auditEventId) {
        return auditService.getById(auditEventId);
    }

    @GetMapping
    public PageResponse<AuditEventResponse> search(
            @RequestParam(required = false) @Size(max = 80) String eventType,
            @RequestParam(required = false) @Size(max = 80) String entityType,
            @RequestParam(required = false) @Size(max = 100) String entityId,
            @RequestParam(required = false) @Size(max = 200) String actor,
            @RequestParam(required = false) @Size(max = 100) String correlationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toTime,
            @PageableDefault(size = 50, sort = "occurredAt") Pageable pageable) {
        return auditService.search(
                eventType,
                entityType,
                entityId,
                actor,
                correlationId,
                fromTime,
                toTime,
                pageable);
    }
}
