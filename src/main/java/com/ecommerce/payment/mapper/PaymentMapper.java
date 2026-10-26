package com.ecommerce.payment.mapper;

import com.ecommerce.payment.dto.PaymentDtos.FraudReviewResponse;
import com.ecommerce.payment.dto.PaymentDtos.PaymentAttemptResponse;
import com.ecommerce.payment.dto.PaymentDtos.PaymentResponse;
import com.ecommerce.payment.dto.PaymentDtos.RefundResponse;
import com.ecommerce.payment.model.FraudReview;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentAttempt;
import com.ecommerce.payment.model.Refund;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getPaidAt(),
                payment.getConfirmation(),
                payment.getCreatedAt(),
                payment.getProcessorTransactionId(),
                payment.getPaymentStatus(),
                payment.getFailureReason());
    }

    public RefundResponse toResponse(Refund refund) {
        return new RefundResponse(
                refund.getRefundId(),
                refund.getPaymentId(),
                refund.getAmount(),
                refund.getReason(),
                refund.getRefundStatus(),
                refund.getProcessorRefundId(),
                refund.getIdempotencyKey(),
                refund.getRequestedAt(),
                refund.getProcessedAt());
    }

    public PaymentAttemptResponse toResponse(PaymentAttempt attempt) {
        return new PaymentAttemptResponse(
                attempt.getPaymentAttemptId(),
                attempt.getPaymentId(),
                attempt.getAttemptNumber(),
                attempt.getIdempotencyKey(),
                attempt.getAttemptStatus(),
                attempt.getProcessorTransactionId(),
                attempt.getFailureReason(),
                attempt.getAttemptedAt());
    }

    public FraudReviewResponse toResponse(FraudReview review) {
        return new FraudReviewResponse(
                review.getFraudReviewId(),
                review.getPaymentId(),
                review.getOrderId(),
                review.getCustomerId(),
                review.getRiskScore(),
                review.getRiskReason(),
                review.getReviewStatus(),
                review.getQueuedAt(),
                review.getReviewedAt(),
                review.getReviewedBy(),
                review.getReviewNotes());
    }
}
