package com.ecommerce.payment.service;

import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.payment.dto.PaymentDtos.FraudReviewDecisionRequest;
import com.ecommerce.payment.dto.PaymentDtos.FraudReviewResponse;
import com.ecommerce.payment.dto.PaymentDtos.PaymentAttemptResponse;
import com.ecommerce.payment.dto.PaymentDtos.PaymentResponse;
import com.ecommerce.payment.dto.PaymentDtos.QueueFraudReviewRequest;
import com.ecommerce.payment.dto.PaymentDtos.RecordPaymentRequest;
import com.ecommerce.payment.dto.PaymentDtos.RefundResponse;
import com.ecommerce.payment.dto.PaymentDtos.RequestRefundRequest;
import com.ecommerce.payment.dto.PaymentDtos.RetryPaymentRequest;
import com.ecommerce.payment.dto.PaymentDtos.UpdatePaymentStatusRequest;
import com.ecommerce.payment.dto.PaymentDtos.UpdateRefundStatusRequest;
import com.ecommerce.payment.gateway.PaymentPackageGateway;
import com.ecommerce.payment.mapper.PaymentMapper;
import com.ecommerce.payment.model.FraudReview;
import com.ecommerce.payment.model.FraudReviewStatus;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.model.Refund;
import com.ecommerce.payment.model.RefundStatus;
import com.ecommerce.payment.repository.FraudReviewRepository;
import com.ecommerce.payment.repository.PaymentAttemptRepository;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.repository.RefundRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class PaymentService {
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int MAX_PAGE_SIZE = 200;
    private static final Set<RefundStatus> RESERVED_REFUND_STATUSES =
            EnumSet.of(
                    RefundStatus.PENDING,
                    RefundStatus.APPROVED,
                    RefundStatus.PROCESSING,
                    RefundStatus.COMPLETED);
    private static final Set<FraudReviewStatus> OPEN_FRAUD_STATUSES =
            EnumSet.of(FraudReviewStatus.PENDING, FraudReviewStatus.IN_REVIEW);
    private static final Map<PaymentStatus, Set<PaymentStatus>> PAYMENT_TRANSITIONS = Map.of(
            PaymentStatus.PENDING,
            EnumSet.of(PaymentStatus.AUTHORIZED, PaymentStatus.FAILED, PaymentStatus.VOIDED),
            PaymentStatus.AUTHORIZED,
            EnumSet.of(PaymentStatus.CAPTURED, PaymentStatus.FAILED, PaymentStatus.VOIDED),
            PaymentStatus.CAPTURED,
            EnumSet.of(
                    PaymentStatus.SETTLED,
                    PaymentStatus.PARTIALLY_REFUNDED,
                    PaymentStatus.REFUNDED),
            PaymentStatus.SETTLED,
            EnumSet.of(PaymentStatus.PARTIALLY_REFUNDED, PaymentStatus.REFUNDED),
            PaymentStatus.FAILED,
            EnumSet.of(PaymentStatus.PENDING, PaymentStatus.AUTHORIZED),
            PaymentStatus.PARTIALLY_REFUNDED,
            EnumSet.of(PaymentStatus.REFUNDED));
    private static final Map<RefundStatus, Set<RefundStatus>> REFUND_TRANSITIONS = Map.of(
            RefundStatus.PENDING,
            EnumSet.of(
                    RefundStatus.APPROVED,
                    RefundStatus.REJECTED,
                    RefundStatus.FAILED,
                    RefundStatus.CANCELLED),
            RefundStatus.APPROVED,
            EnumSet.of(
                    RefundStatus.PROCESSING,
                    RefundStatus.REJECTED,
                    RefundStatus.FAILED,
                    RefundStatus.CANCELLED),
            RefundStatus.PROCESSING,
            EnumSet.of(RefundStatus.COMPLETED, RefundStatus.FAILED));

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final FraudReviewRepository fraudReviewRepository;
    private final PaymentPackageGateway paymentGateway;
    private final PaymentMapper paymentMapper;
    private final AuditService auditService;

    public PaymentService(
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            FraudReviewRepository fraudReviewRepository,
            PaymentPackageGateway paymentGateway,
            PaymentMapper paymentMapper,
            AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.fraudReviewRepository = fraudReviewRepository;
        this.paymentGateway = paymentGateway;
        this.paymentMapper = paymentMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        return paymentMapper.toResponse(requirePayment(paymentId));
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> findPayments(
            Long orderId,
            Long customerId,
            PaymentStatus status,
            Pageable pageable) {
        validatePage(pageable);
        return PageResponse.from(
                paymentRepository.search(orderId, customerId, status, pageable),
                paymentMapper::toResponse);
    }

    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest request) {
        validateRecordedPayment(request);
        if (paymentRepository.existsByProcessorTransactionId(request.processorTransactionId())) {
            throw new BusinessRuleException(
                    "DUPLICATE_PROCESSOR_TRANSACTION",
                    "Processor transaction has already been recorded");
        }
        Long paymentId = paymentGateway.recordPayment(request);
        Payment payment = requirePayment(paymentId);
        audit(
                "PAYMENT_RECORDED",
                "Payment",
                paymentId,
                "orderId=" + payment.getOrderId() + ",status=" + payment.getPaymentStatus());
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse updateStatus(Long paymentId, UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        validatePaymentTransition(payment, request);
        paymentGateway.updatePaymentStatus(paymentId, request);
        Payment updated = requirePayment(paymentId);
        audit(
                "PAYMENT_STATUS_CHANGED",
                "Payment",
                paymentId,
                "from=" + payment.getPaymentStatus() + ",to=" + request.status());
        return paymentMapper.toResponse(updated);
    }

    @Transactional
    public PaymentAttemptResponse retryPayment(Long paymentId, RetryPaymentRequest request) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        if (payment.getPaymentStatus() != PaymentStatus.FAILED) {
            throw new BusinessRuleException(
                    "PAYMENT_NOT_RETRYABLE",
                    "Only failed payments can be retried");
        }
        if (paymentAttemptRepository.existsByIdempotencyKey(request.idempotencyKey())) {
            throw new BusinessRuleException(
                    "DUPLICATE_PAYMENT_RETRY",
                    "A payment attempt already uses this idempotency key");
        }
        long attempts = paymentAttemptRepository.countByPaymentId(paymentId);
        if (attempts >= MAX_RETRY_ATTEMPTS) {
            throw new BusinessRuleException(
                    "PAYMENT_RETRY_LIMIT",
                    "Payment retry limit of " + MAX_RETRY_ATTEMPTS + " has been reached");
        }
        Long attemptId = paymentGateway.retryPayment(paymentId, request.idempotencyKey());
        var attempt = paymentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt", attemptId));
        audit(
                "PAYMENT_RETRIED",
                "Payment",
                paymentId,
                "attemptId=" + attemptId + ",attemptNumber=" + attempt.getAttemptNumber());
        return paymentMapper.toResponse(attempt);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentAttemptResponse> findAttempts(Long paymentId, Pageable pageable) {
        requirePayment(paymentId);
        validatePage(pageable);
        return PageResponse.from(
                paymentAttemptRepository.findByPaymentId(paymentId, pageable),
                paymentMapper::toResponse);
    }

    @Transactional
    public RefundResponse requestRefund(Long paymentId, RequestRefundRequest request) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        if (!payment.getPaymentStatus().permitsRefund()) {
            throw new BusinessRuleException(
                    "PAYMENT_NOT_REFUNDABLE",
                    "Payment must be captured or settled before it can be refunded");
        }
        if (refundRepository.existsByIdempotencyKey(request.idempotencyKey())) {
            throw new BusinessRuleException(
                    "DUPLICATE_REFUND_REQUEST",
                    "A refund already uses this idempotency key");
        }
        BigDecimal reserved = refundRepository.sumReservedAmount(paymentId, RESERVED_REFUND_STATUSES);
        if (reserved.add(request.amount()).compareTo(payment.getAmount()) > 0) {
            throw new BusinessRuleException(
                    "REFUND_EXCEEDS_CAPTURED_AMOUNT",
                    "Requested and reserved refunds cannot exceed the captured payment amount");
        }
        Long refundId = paymentGateway.requestRefund(paymentId, request);
        Refund refund = requireRefund(refundId);
        audit(
                "REFUND_REQUESTED",
                "Refund",
                refundId,
                "paymentId=" + paymentId + ",amount=" + request.amount());
        return paymentMapper.toResponse(refund);
    }

    @Transactional
    public RefundResponse updateRefundStatus(Long refundId, UpdateRefundStatusRequest request) {
        Refund refund = refundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", refundId));
        Set<RefundStatus> permitted = REFUND_TRANSITIONS.getOrDefault(
                refund.getRefundStatus(),
                Set.of());
        if (!permitted.contains(request.status())) {
            throw new BusinessRuleException(
                    "INVALID_REFUND_TRANSITION",
                    "Refund cannot transition from "
                            + refund.getRefundStatus()
                            + " to "
                            + request.status());
        }
        if (request.status() == RefundStatus.COMPLETED
                && (request.processorRefundId() == null || request.processorRefundId().isBlank())) {
            throw new BusinessRuleException(
                    "PROCESSOR_REFUND_ID_REQUIRED",
                    "A completed refund requires a processor refund identifier");
        }
        paymentGateway.updateRefundStatus(refundId, request);
        Refund updated = requireRefund(refundId);
        audit(
                "REFUND_STATUS_CHANGED",
                "Refund",
                refundId,
                "from=" + refund.getRefundStatus() + ",to=" + request.status());
        return paymentMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public PageResponse<RefundResponse> findRefunds(Long paymentId, Pageable pageable) {
        requirePayment(paymentId);
        validatePage(pageable);
        return PageResponse.from(
                refundRepository.findByPaymentId(paymentId, pageable),
                paymentMapper::toResponse);
    }

    @Transactional
    public FraudReviewResponse queueFraudReview(
            Long paymentId,
            QueueFraudReviewRequest request) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        if (payment.getPaymentStatus() != PaymentStatus.PENDING
                && payment.getPaymentStatus() != PaymentStatus.AUTHORIZED) {
            throw new BusinessRuleException(
                    "PAYMENT_NOT_REVIEWABLE",
                    "Fraud review must occur before payment capture");
        }
        if (fraudReviewRepository.existsByPaymentIdAndReviewStatusIn(
                paymentId,
                OPEN_FRAUD_STATUSES)) {
            throw new BusinessRuleException(
                    "FRAUD_REVIEW_ALREADY_OPEN",
                    "Payment already has an open fraud review");
        }
        Long reviewId = paymentGateway.queueFraudReview(
                paymentId,
                payment.getOrderId(),
                payment.getCustomerId(),
                request);
        FraudReview review = requireFraudReview(reviewId);
        audit(
                "FRAUD_REVIEW_QUEUED",
                "FraudReview",
                reviewId,
                "paymentId=" + paymentId + ",riskScore=" + request.riskScore());
        return paymentMapper.toResponse(review);
    }

    @Transactional
    public FraudReviewResponse reviewFraud(
            Long fraudReviewId,
            FraudReviewDecisionRequest request) {
        FraudReview review = requireFraudReview(fraudReviewId);
        if (!review.getReviewStatus().isOpen()) {
            throw new BusinessRuleException(
                    "FRAUD_REVIEW_CLOSED",
                    "Only open fraud reviews can be decided");
        }
        paymentGateway.reviewFraud(fraudReviewId, request);
        FraudReview updated = requireFraudReview(fraudReviewId);
        audit(
                "FRAUD_REVIEW_DECIDED",
                "FraudReview",
                fraudReviewId,
                "decision=" + request.decision() + ",paymentId=" + review.getPaymentId());
        return paymentMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public PageResponse<FraudReviewResponse> findFraudReviews(
            FraudReviewStatus status,
            Long customerId,
            Pageable pageable) {
        validatePage(pageable);
        return PageResponse.from(
                fraudReviewRepository.search(status, customerId, pageable),
                paymentMapper::toResponse);
    }

    private Payment requirePayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
    }

    private Refund requireRefund(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", refundId));
    }

    private FraudReview requireFraudReview(Long fraudReviewId) {
        return fraudReviewRepository.findById(fraudReviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud review", fraudReviewId));
    }

    private void validateRecordedPayment(RecordPaymentRequest request) {
        if (request.paymentStatus() == PaymentStatus.FAILED) {
            if (request.failureReason() == null || request.failureReason().isBlank()) {
                throw new BusinessRuleException(
                        "PAYMENT_FAILURE_REASON_REQUIRED",
                        "Failed payment records require a failure reason");
            }
        } else if (request.failureReason() != null && !request.failureReason().isBlank()) {
            throw new BusinessRuleException(
                    "UNEXPECTED_PAYMENT_FAILURE_REASON",
                    "Failure reason is only valid for failed payments");
        }
        if ((request.paymentStatus() == PaymentStatus.CAPTURED
                || request.paymentStatus() == PaymentStatus.SETTLED)
                && request.paidAt() == null) {
            throw new BusinessRuleException(
                    "PAYMENT_TIME_REQUIRED",
                    "Captured or settled payments require paidAt");
        }
        Set<PaymentStatus> initialStatuses = EnumSet.of(
                PaymentStatus.PENDING,
                PaymentStatus.AUTHORIZED,
                PaymentStatus.CAPTURED,
                PaymentStatus.SETTLED,
                PaymentStatus.FAILED);
        if (!initialStatuses.contains(request.paymentStatus())) {
            throw new BusinessRuleException(
                    "INVALID_INITIAL_PAYMENT_STATUS",
                    "Payment cannot be recorded initially as " + request.paymentStatus());
        }
    }

    private void validatePaymentTransition(
            Payment payment,
            UpdatePaymentStatusRequest request) {
        Set<PaymentStatus> permitted = PAYMENT_TRANSITIONS.getOrDefault(
                payment.getPaymentStatus(),
                Set.of());
        if (!permitted.contains(request.status())) {
            throw new BusinessRuleException(
                    "INVALID_PAYMENT_TRANSITION",
                    "Payment cannot transition from "
                            + payment.getPaymentStatus()
                            + " to "
                            + request.status());
        }
        if (request.status() == PaymentStatus.FAILED
                && (request.failureReason() == null || request.failureReason().isBlank())) {
            throw new BusinessRuleException(
                    "PAYMENT_FAILURE_REASON_REQUIRED",
                    "Failed payments require a failure reason");
        }
        if ((request.status() == PaymentStatus.CAPTURED
                || request.status() == PaymentStatus.SETTLED)
                && request.paidAt() == null) {
            throw new BusinessRuleException(
                    "PAYMENT_TIME_REQUIRED",
                    "Captured or settled payments require paidAt");
        }
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Payment page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private void audit(String eventType, String entityType, Long entityId, String payload) {
        auditService.record(new WriteAuditEventRequest(
                eventType,
                entityType,
                entityId.toString(),
                currentActor(),
                MDC.get("correlationId"),
                payload,
                null));
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }
}
