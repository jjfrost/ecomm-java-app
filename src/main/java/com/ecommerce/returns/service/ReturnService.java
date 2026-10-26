package com.ecommerce.returns.service;

import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.returns.dto.ReturnDtos.AuthorizeReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.CreateReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.ReceiveReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.ReturnDetailResponse;
import com.ecommerce.returns.dto.ReturnDtos.ReturnRequestResponse;
import com.ecommerce.returns.dto.ReturnDtos.SetDispositionRequest;
import com.ecommerce.returns.gateway.ReturnPackageGateway;
import com.ecommerce.returns.mapper.ReturnMapper;
import com.ecommerce.returns.model.ReturnDisposition;
import com.ecommerce.returns.model.ReturnItem;
import com.ecommerce.returns.model.ReturnRequest;
import com.ecommerce.returns.model.ReturnStatus;
import com.ecommerce.returns.repository.ReturnItemRepository;
import com.ecommerce.returns.repository.ReturnRequestRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Service
public class ReturnService {
    private static final int MAX_PAGE_SIZE = 200;
    private static final Set<ReturnStatus> OPEN_STATUSES = EnumSet.of(
            ReturnStatus.REQUESTED,
            ReturnStatus.AUTHORIZED,
            ReturnStatus.IN_TRANSIT,
            ReturnStatus.RECEIVED,
            ReturnStatus.INSPECTED);

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnItemRepository returnItemRepository;
    private final ReturnPackageGateway returnGateway;
    private final ReturnMapper returnMapper;
    private final AuditService auditService;

    public ReturnService(
            ReturnRequestRepository returnRequestRepository,
            ReturnItemRepository returnItemRepository,
            ReturnPackageGateway returnGateway,
            ReturnMapper returnMapper,
            AuditService auditService) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnItemRepository = returnItemRepository;
        this.returnGateway = returnGateway;
        this.returnMapper = returnMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public ReturnDetailResponse getReturn(Long returnRequestId) {
        ReturnRequest request = requireReturn(returnRequestId);
        var items = returnItemRepository.findByReturnRequestIdOrderByReturnItemId(returnRequestId)
                .stream()
                .map(returnMapper::toResponse)
                .toList();
        return new ReturnDetailResponse(returnMapper.toResponse(request), items);
    }

    @Transactional(readOnly = true)
    public ReturnDetailResponse getByAuthorizationCode(String authorizationCode) {
        ReturnRequest request = returnRequestRepository.findByAuthorizationCode(authorizationCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Return authorization " + authorizationCode + " was not found"));
        return getReturn(request.getReturnRequestId());
    }

    @Transactional(readOnly = true)
    public PageResponse<ReturnRequestResponse> findReturns(
            Long orderId,
            Long customerId,
            ReturnStatus status,
            Pageable pageable) {
        validatePage(pageable);
        return PageResponse.from(
                returnRequestRepository.search(orderId, customerId, status, pageable),
                returnMapper::toResponse);
    }

    @Transactional
    public ReturnDetailResponse createReturn(CreateReturnRequest request) {
        Set<Long> uniqueItems = new HashSet<>();
        for (var item : request.items()) {
            if (!uniqueItems.add(item.orderItemId())) {
                throw new BusinessRuleException(
                        "DUPLICATE_RETURN_ITEM",
                        "Each order item can appear only once in a return request");
            }
        }
        if (returnItemRepository.countOpenItems(uniqueItems, OPEN_STATUSES) > 0) {
            throw new BusinessRuleException(
                    "ORDER_ITEM_RETURN_OPEN",
                    "At least one order item already belongs to an open return");
        }
        Long returnRequestId = returnGateway.createReturn(request);
        request.items().forEach(item -> returnGateway.addReturnItem(returnRequestId, item));
        audit(
                "RETURN_REQUESTED",
                returnRequestId,
                "orderId=" + request.orderId() + ",itemCount=" + request.items().size());
        return getReturn(returnRequestId);
    }

    @Transactional
    public ReturnRequestResponse authorize(
            Long returnRequestId,
            AuthorizeReturnRequest request) {
        ReturnRequest returnRequest = lockReturn(returnRequestId);
        requireState(returnRequest, Set.of(ReturnStatus.REQUESTED), "authorize");
        returnGateway.authorize(returnRequestId, request);
        audit(
                request.approved() ? "RETURN_AUTHORIZED" : "RETURN_REJECTED",
                returnRequestId,
                "reviewedBy=" + request.reviewedBy());
        return returnMapper.toResponse(requireReturn(returnRequestId));
    }

    @Transactional
    public ReturnRequestResponse receive(
            Long returnRequestId,
            ReceiveReturnRequest request) {
        ReturnRequest returnRequest = lockReturn(returnRequestId);
        requireState(
                returnRequest,
                Set.of(ReturnStatus.AUTHORIZED, ReturnStatus.IN_TRANSIT),
                "receive");
        if (request.receivedAt().isAfter(Instant.now())) {
            throw new BusinessRuleException(
                    "RETURN_RECEIPT_IN_FUTURE",
                    "Return receipt time cannot be in the future");
        }
        returnGateway.receive(returnRequestId, request);
        audit(
                "RETURN_RECEIVED",
                returnRequestId,
                "receivedAt=" + request.receivedAt() + ",receivedBy=" + request.receivedBy());
        return returnMapper.toResponse(requireReturn(returnRequestId));
    }

    @Transactional
    public ReturnDetailResponse setDisposition(
            Long returnRequestId,
            Long returnItemId,
            SetDispositionRequest request) {
        ReturnRequest returnRequest = lockReturn(returnRequestId);
        requireState(
                returnRequest,
                Set.of(ReturnStatus.RECEIVED, ReturnStatus.INSPECTED),
                "set item disposition");
        ReturnItem item = returnItemRepository
                .findByReturnItemIdAndReturnRequestId(returnItemId, returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Return item " + returnItemId + " does not belong to return " + returnRequestId));
        validateDisposition(request);
        returnGateway.setDisposition(returnRequestId, item.getReturnItemId(), request);
        audit(
                "RETURN_ITEM_DISPOSITIONED",
                returnRequestId,
                "returnItemId=" + returnItemId + ",disposition=" + request.disposition());
        return getReturn(returnRequestId);
    }

    @Transactional
    public ReturnRequestResponse complete(Long returnRequestId) {
        ReturnRequest returnRequest = lockReturn(returnRequestId);
        requireState(
                returnRequest,
                Set.of(ReturnStatus.RECEIVED, ReturnStatus.INSPECTED),
                "complete");
        long pendingItems = returnItemRepository.countByReturnRequestIdAndDisposition(
                returnRequestId,
                ReturnDisposition.PENDING);
        if (pendingItems > 0) {
            throw new BusinessRuleException(
                    "RETURN_ITEMS_PENDING",
                    "Every return item requires a disposition before completion");
        }
        returnGateway.complete(returnRequestId);
        audit("RETURN_COMPLETED", returnRequestId, "orderId=" + returnRequest.getOrderId());
        return returnMapper.toResponse(requireReturn(returnRequestId));
    }

    @Transactional
    public ReturnRequestResponse cancel(Long returnRequestId, String reason) {
        ReturnRequest returnRequest = lockReturn(returnRequestId);
        requireState(
                returnRequest,
                Set.of(ReturnStatus.REQUESTED, ReturnStatus.AUTHORIZED),
                "cancel");
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException(
                    "RETURN_CANCELLATION_REASON_REQUIRED",
                    "Return cancellation requires a reason");
        }
        returnGateway.cancel(returnRequestId, reason.trim());
        audit("RETURN_CANCELLED", returnRequestId, "reason=" + reason.trim());
        return returnMapper.toResponse(requireReturn(returnRequestId));
    }

    private void validateDisposition(SetDispositionRequest request) {
        if (request.disposition() == ReturnDisposition.PENDING) {
            throw new BusinessRuleException(
                    "INVALID_RETURN_DISPOSITION",
                    "PENDING is not a final item disposition");
        }
        BigDecimal refundAmount = request.refundAmount();
        if (request.disposition() == ReturnDisposition.REJECTED) {
            if (refundAmount != null && refundAmount.signum() > 0) {
                throw new BusinessRuleException(
                        "REJECTED_ITEM_REFUND",
                        "Rejected return items cannot have a refund amount");
            }
        } else if (refundAmount == null) {
            throw new BusinessRuleException(
                    "REFUND_AMOUNT_REQUIRED",
                    "Accepted return items require a refund amount, including zero");
        }
    }

    private ReturnRequest lockReturn(Long returnRequestId) {
        return returnRequestRepository.findByIdForUpdate(returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request", returnRequestId));
    }

    private ReturnRequest requireReturn(Long returnRequestId) {
        return returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Return request", returnRequestId));
    }

    private void requireState(
            ReturnRequest request,
            Set<ReturnStatus> expected,
            String operation) {
        if (!expected.contains(request.getReturnStatus())) {
            throw new BusinessRuleException(
                    "INVALID_RETURN_STATE",
                    "Cannot " + operation + " return in " + request.getReturnStatus() + " state");
        }
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Return page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private void audit(String eventType, Long returnRequestId, String payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null ? "system" : authentication.getName();
        auditService.record(new WriteAuditEventRequest(
                eventType,
                "ReturnRequest",
                returnRequestId.toString(),
                actor,
                MDC.get("correlationId"),
                payload,
                null));
    }
}
