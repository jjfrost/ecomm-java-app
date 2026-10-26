package com.ecommerce.returns.dto;

import com.ecommerce.returns.model.ReturnDisposition;
import com.ecommerce.returns.model.ReturnReason;
import com.ecommerce.returns.model.ReturnStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ReturnDtos {
    private ReturnDtos() {
    }

    public record CreateReturnRequest(
            @NotNull @Positive Long orderId,
            @NotNull @Positive Long customerId,
            @NotNull ReturnReason reason,
            @Size(max = 2000) String customerNotes,
            @NotEmpty @Size(max = 100) List<@Valid ReturnItemRequest> items) {
    }

    public record ReturnItemRequest(
            @NotNull @Positive Long orderItemId,
            @NotNull @Positive Long productId,
            @NotNull @Positive Integer quantity) {
    }

    public record AuthorizeReturnRequest(
            @NotNull Boolean approved,
            @NotBlank @Size(max = 200) String reviewedBy,
            @NotBlank @Size(max = 1000) String reviewNotes) {
    }

    public record ReceiveReturnRequest(
            @NotNull Instant receivedAt,
            @NotBlank @Size(max = 200) String receivedBy) {
    }

    public record SetDispositionRequest(
            @NotNull ReturnDisposition disposition,
            @NotBlank @Size(max = 200) String itemCondition,
            @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal refundAmount) {
    }

    public record ReturnRequestResponse(
            Long returnRequestId,
            Long orderId,
            Long customerId,
            ReturnReason returnReason,
            ReturnStatus returnStatus,
            String authorizationCode,
            String customerNotes,
            Instant requestedAt,
            Instant authorizedAt,
            Instant receivedAt,
            Instant completedAt) {
    }

    public record ReturnItemResponse(
            Long returnItemId,
            Long returnRequestId,
            Long orderItemId,
            Long productId,
            Integer quantity,
            String itemCondition,
            ReturnDisposition disposition,
            BigDecimal refundAmount) {
    }

    public record ReturnDetailResponse(
            ReturnRequestResponse request,
            List<ReturnItemResponse> items) {
    }
}
