package com.ecommerce.payment.web;

import com.ecommerce.common.api.PageResponse;
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
import com.ecommerce.payment.model.FraudReviewStatus;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public PaymentResponse getPayment(@PathVariable @Positive Long paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<PaymentResponse> findPayments(
            @RequestParam(required = false) @Positive Long orderId,
            @RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 25, sort = "createdAt") Pageable pageable) {
        return paymentService.findPayments(orderId, customerId, status, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<PaymentResponse> recordPayment(
            @Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.recordPayment(request));
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PaymentResponse updateStatus(
            @PathVariable @Positive Long paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return paymentService.updateStatus(paymentId, request);
    }

    @PostMapping("/{paymentId}/attempts")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<PaymentAttemptResponse> retryPayment(
            @PathVariable @Positive Long paymentId,
            @Valid @RequestBody RetryPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.retryPayment(paymentId, request));
    }

    @GetMapping("/{paymentId}/attempts")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<PaymentAttemptResponse> findAttempts(
            @PathVariable @Positive Long paymentId,
            @PageableDefault(size = 25, sort = "attemptedAt") Pageable pageable) {
        return paymentService.findAttempts(paymentId, pageable);
    }

    @PostMapping("/{paymentId}/refunds")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<RefundResponse> requestRefund(
            @PathVariable @Positive Long paymentId,
            @Valid @RequestBody RequestRefundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.requestRefund(paymentId, request));
    }

    @GetMapping("/{paymentId}/refunds")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<RefundResponse> findRefunds(
            @PathVariable @Positive Long paymentId,
            @PageableDefault(size = 25, sort = "requestedAt") Pageable pageable) {
        return paymentService.findRefunds(paymentId, pageable);
    }

    @PatchMapping("/refunds/{refundId}/status")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public RefundResponse updateRefundStatus(
            @PathVariable @Positive Long refundId,
            @Valid @RequestBody UpdateRefundStatusRequest request) {
        return paymentService.updateRefundStatus(refundId, request);
    }

    @PostMapping("/{paymentId}/fraud-reviews")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<FraudReviewResponse> queueFraudReview(
            @PathVariable @Positive Long paymentId,
            @Valid @RequestBody QueueFraudReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.queueFraudReview(paymentId, request));
    }

    @PatchMapping("/fraud-reviews/{fraudReviewId}/decision")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public FraudReviewResponse reviewFraud(
            @PathVariable @Positive Long fraudReviewId,
            @Valid @RequestBody FraudReviewDecisionRequest request) {
        return paymentService.reviewFraud(fraudReviewId, request);
    }

    @GetMapping("/fraud-reviews")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<FraudReviewResponse> findFraudReviews(
            @RequestParam(required = false) FraudReviewStatus status,
            @RequestParam(required = false) @Positive Long customerId,
            @PageableDefault(size = 25, sort = "queuedAt") Pageable pageable) {
        return paymentService.findFraudReviews(status, customerId, pageable);
    }
}
