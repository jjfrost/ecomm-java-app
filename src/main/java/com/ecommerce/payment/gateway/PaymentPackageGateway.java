package com.ecommerce.payment.gateway;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.payment.dto.PaymentDtos.FraudReviewDecisionRequest;
import com.ecommerce.payment.dto.PaymentDtos.QueueFraudReviewRequest;
import com.ecommerce.payment.dto.PaymentDtos.RecordPaymentRequest;
import com.ecommerce.payment.dto.PaymentDtos.RequestRefundRequest;
import com.ecommerce.payment.dto.PaymentDtos.UpdatePaymentStatusRequest;
import com.ecommerce.payment.dto.PaymentDtos.UpdateRefundStatusRequest;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;

@Component
public class PaymentPackageGateway {
    private static final String RECORD_PAYMENT = "ecomm.PAYMENT_PKG.record_payment";
    private static final String UPDATE_PAYMENT_STATUS = "ecomm.PAYMENT_PKG.update_payment_status";
    private static final String RETRY_PAYMENT = "ecomm.PAYMENT_PKG.retry_payment";
    private static final String REQUEST_REFUND = "ecomm.PAYMENT_PKG.request_refund";
    private static final String UPDATE_REFUND_STATUS = "ecomm.PAYMENT_PKG.update_refund_status";
    private static final String QUEUE_FRAUD_REVIEW = "ecomm.PAYMENT_PKG.queue_fraud_review";
    private static final String REVIEW_FRAUD = "ecomm.PAYMENT_PKG.review_fraud";

    private final OraclePackageExecutor packageExecutor;
    private final EntityManager entityManager;

    public PaymentPackageGateway(
            OraclePackageExecutor packageExecutor,
            EntityManager entityManager) {
        this.packageExecutor = packageExecutor;
        this.entityManager = entityManager;
    }

    public Long recordPayment(RecordPaymentRequest request) {
        BigDecimal result = packageExecutor.executeFunction(
                RECORD_PAYMENT,
                "{? = call ecomm.PAYMENT_PKG.record_payment(?, ?, ?, ?, ?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, request.orderId());
                    statement.setLong(3, request.customerId());
                    statement.setString(4, request.paymentMethod().name());
                    statement.setBigDecimal(5, request.amount());
                    if (request.paidAt() == null) {
                        statement.setNull(6, Types.TIMESTAMP);
                    } else {
                        statement.setTimestamp(6, Timestamp.from(request.paidAt()));
                    }
                    statement.setString(7, request.confirmation());
                    statement.setString(8, request.processorTransactionId());
                    statement.setString(9, request.paymentStatus().name());
                    statement.setString(10, request.failureReason());
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request) {
        packageExecutor.execute(
                UPDATE_PAYMENT_STATUS,
                "{call ecomm.PAYMENT_PKG.update_payment_status(?, ?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, paymentId);
                    statement.setString(2, request.status().name());
                    statement.setString(3, request.confirmation());
                    statement.setString(4, request.failureReason());
                    if (request.paidAt() == null) {
                        statement.setNull(5, Types.TIMESTAMP);
                    } else {
                        statement.setTimestamp(5, Timestamp.from(request.paidAt()));
                    }
                });
        entityManager.clear();
    }

    public Long retryPayment(Long paymentId, String idempotencyKey) {
        BigDecimal result = packageExecutor.executeFunction(
                RETRY_PAYMENT,
                "{? = call ecomm.PAYMENT_PKG.retry_payment(?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, paymentId);
                    statement.setString(3, idempotencyKey);
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public Long requestRefund(Long paymentId, RequestRefundRequest request) {
        BigDecimal result = packageExecutor.executeFunction(
                REQUEST_REFUND,
                "{? = call ecomm.PAYMENT_PKG.request_refund(?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, paymentId);
                    statement.setBigDecimal(3, request.amount());
                    statement.setString(4, request.reason());
                    statement.setString(5, request.idempotencyKey());
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void updateRefundStatus(Long refundId, UpdateRefundStatusRequest request) {
        packageExecutor.execute(
                UPDATE_REFUND_STATUS,
                "{call ecomm.PAYMENT_PKG.update_refund_status(?, ?, ?)}",
                statement -> {
                    statement.setLong(1, refundId);
                    statement.setString(2, request.status().name());
                    statement.setString(3, request.processorRefundId());
                });
        entityManager.clear();
    }

    public Long queueFraudReview(
            Long paymentId,
            Long orderId,
            Long customerId,
            QueueFraudReviewRequest request) {
        BigDecimal result = packageExecutor.executeFunction(
                QUEUE_FRAUD_REVIEW,
                "{? = call ecomm.PAYMENT_PKG.queue_fraud_review(?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, paymentId);
                    statement.setLong(3, orderId);
                    statement.setLong(4, customerId);
                    statement.setBigDecimal(5, request.riskScore());
                    statement.setString(6, request.riskReason());
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void reviewFraud(Long fraudReviewId, FraudReviewDecisionRequest request) {
        packageExecutor.execute(
                REVIEW_FRAUD,
                "{call ecomm.PAYMENT_PKG.review_fraud(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, fraudReviewId);
                    statement.setString(2, request.decision().name());
                    statement.setString(3, request.reviewer());
                    statement.setString(4, request.notes());
                });
        entityManager.clear();
    }
}
