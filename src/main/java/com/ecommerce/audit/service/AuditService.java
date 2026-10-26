package com.ecommerce.audit.service;

import com.ecommerce.audit.dto.AuditDtos.AuditEventResponse;
import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.mapper.AuditMapper;
import com.ecommerce.audit.model.AuditEvent;
import com.ecommerce.audit.repository.AuditEventRepository;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditService {
    private static final int MAX_PAGE_SIZE = 200;

    private final AuditEventRepository auditEventRepository;
    private final AuditMapper auditMapper;

    public AuditService(AuditEventRepository auditEventRepository, AuditMapper auditMapper) {
        this.auditEventRepository = auditEventRepository;
        this.auditMapper = auditMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public AuditEventResponse record(WriteAuditEventRequest request) {
        AuditEvent event = auditMapper.toEntity(request);
        return auditMapper.toResponse(auditEventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public AuditEventResponse getById(Long auditEventId) {
        return auditMapper.toResponse(auditEventRepository.findById(auditEventId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit event", auditEventId)));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> search(
            String eventType,
            String entityType,
            String entityId,
            String actor,
            String correlationId,
            Instant fromTime,
            Instant toTime,
            Pageable pageable) {
        validateRange(fromTime, toTime);
        validatePage(pageable);
        return PageResponse.from(
                auditEventRepository.search(
                        normalize(eventType),
                        normalize(entityType),
                        normalize(entityId),
                        normalize(actor),
                        normalize(correlationId),
                        fromTime,
                        toTime,
                        pageable),
                auditMapper::toResponse);
    }

    private void validateRange(Instant fromTime, Instant toTime) {
        if (fromTime != null && toTime != null && !fromTime.isBefore(toTime)) {
            throw new BusinessRuleException(
                    "INVALID_AUDIT_RANGE",
                    "Audit search start time must be before end time");
        }
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Audit page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
