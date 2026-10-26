package com.ecommerce.payment.service;

import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.payment.dto.PaymentDtos.PaymentResponse;
import com.ecommerce.payment.dto.PaymentDtos.RecordPaymentRequest;
import com.ecommerce.payment.gateway.PaymentPackageGateway;
import com.ecommerce.payment.mapper.PaymentMapper;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentMethod;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.FraudReviewRepository;
import com.ecommerce.payment.repository.PaymentAttemptRepository;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.repository.RefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private FraudReviewRepository fraudReviewRepository;

    @Mock
    private PaymentPackageGateway paymentGateway;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private AuditService auditService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                refundRepository,
                paymentAttemptRepository,
                fraudReviewRepository,
                paymentGateway,
                paymentMapper,
                auditService);
    }

    @Test
    void recordsValidatedPaymentThroughGatewayAndAuditsIt() {
        Instant paidAt = Instant.parse("2026-10-04T18:00:00Z");
        RecordPaymentRequest request = new RecordPaymentRequest(
                41L,
                12L,
                PaymentMethod.CARD,
                new BigDecimal("89.50"),
                paidAt,
                "CONF-91",
                "processor-91",
                PaymentStatus.CAPTURED,
                null);
        Payment payment = org.mockito.Mockito.mock(Payment.class);
        PaymentResponse response = new PaymentResponse(
                91L,
                41L,
                12L,
                PaymentMethod.CARD,
                new BigDecimal("89.50"),
                paidAt,
                "CONF-91",
                paidAt,
                "processor-91",
                PaymentStatus.CAPTURED,
                null);
        when(paymentRepository.existsByProcessorTransactionId("processor-91")).thenReturn(false);
        when(paymentGateway.recordPayment(request)).thenReturn(91L);
        when(paymentRepository.findById(91L)).thenReturn(Optional.of(payment));
        when(payment.getOrderId()).thenReturn(41L);
        when(payment.getPaymentStatus()).thenReturn(PaymentStatus.CAPTURED);
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.recordPayment(request);

        assertSame(response, result);
        verify(paymentGateway).recordPayment(request);
        ArgumentCaptor<WriteAuditEventRequest> audit =
                ArgumentCaptor.forClass(WriteAuditEventRequest.class);
        verify(auditService).record(audit.capture());
        assertEquals("PAYMENT_RECORDED", audit.getValue().eventType());
        assertEquals("91", audit.getValue().entityId());
    }
}
