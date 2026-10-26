package com.ecommerce.ui.form;

import com.ecommerce.payment.dto.PaymentDtos.UpdatePaymentStatusRequest;
import com.ecommerce.payment.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class PaymentStatusForm {
    @NotNull
    private PaymentStatus status;

    @Size(max = 100)
    private String confirmation;

    @Size(max = 500)
    private String failureReason;

    private LocalDateTime paidAt;

    public UpdatePaymentStatusRequest toRequest() {
        Instant instant = paidAt == null ? null : paidAt.toInstant(ZoneOffset.UTC);
        return new UpdatePaymentStatusRequest(status, confirmation, failureReason, instant);
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
