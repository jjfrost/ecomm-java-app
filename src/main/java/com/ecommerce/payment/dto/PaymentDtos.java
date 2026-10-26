package com.ecommerce.payment.dto;

import com.ecommerce.payment.model.FraudDecision;
import com.ecommerce.payment.model.FraudReviewStatus;
import com.ecommerce.payment.model.PaymentAttemptStatus;
import com.ecommerce.payment.model.PaymentMethod;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.model.RefundStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    public record RecordPaymentRequest(
            @NotNull @Positive Long orderId,
            @NotNull @Positive Long customerId,
            @NotNull PaymentMethod paymentMethod,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            Instant paidAt,
            @Size(max = 100) String confirmation,
            @NotBlank @Size(max = 150) String processorTransactionId,
            @NotNull PaymentStatus paymentStatus,
            @Size(max = 500) String failureReason) {
    }

    public record UpdatePaymentStatusRequest(
            @NotNull PaymentStatus status,
            @Size(max = 100) String confirmation,
            @Size(max = 500) String failureReason,
            Instant paidAt) {
    }

    public record RetryPaymentRequest(
            @NotBlank @Size(max = 100) String idempotencyKey) {
    }

    public record RequestRefundRequest(
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            @NotBlank @Size(max = 500) String reason,
            @NotBlank @Size(max = 100) String idempotencyKey) {
    }

    public record UpdateRefundStatusRequest(
            @NotNull RefundStatus status,
            @Size(max = 150) String processorRefundId) {
    }

    public record QueueFraudReviewRequest(
            @NotNull @DecimalMin("0.00") @DecimalMax("100.00")
            @Digits(integer = 3, fraction = 2) BigDecimal riskScore,
            @NotBlank @Size(max = 1000) String riskReason) {
    }

    public record FraudReviewDecisionRequest(
            @NotNull FraudDecision decision,
            @NotBlank @Size(max = 200) String reviewer,
            @NotBlank @Size(max = 2000) String notes) {
    }

    public record PaymentResponse(
            Long paymentId,
            Long orderId,
            Long customerId,
            PaymentMethod paymentMethod,
            BigDecimal amount,
            Instant paidAt,
            String confirmation,
            Instant createdAt,
            String processorTransactionId,
            PaymentStatus paymentStatus,
            String failureReason) {
    }

    public record RefundResponse(
            Long refundId,
            Long paymentId,
            BigDecimal amount,
            String reason,
            RefundStatus refundStatus,
            String processorRefundId,
            String idempotencyKey,
            Instant requestedAt,
            Instant processedAt) {
    }

    public record PaymentAttemptResponse(
            Long paymentAttemptId,
            Long paymentId,
            Integer attemptNumber,
            String idempotencyKey,
            PaymentAttemptStatus attemptStatus,
            String processorTransactionId,
            String failureReason,
            Instant attemptedAt) {
    }

    public record FraudReviewResponse(
            Long fraudReviewId,
            Long paymentId,
            Long orderId,
            Long customerId,
            BigDecimal riskScore,
            String riskReason,
            FraudReviewStatus reviewStatus,
            Instant queuedAt,
            Instant reviewedAt,
            String reviewedBy,
            String reviewNotes) {
    }
}
