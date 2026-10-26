package com.ecommerce.supplier.dto;

import com.ecommerce.supplier.model.PurchaseOrderStatus;
import com.ecommerce.supplier.model.SupplierStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class SupplierDtos {
    private SupplierDtos() {
    }

    public record CreateSupplierRequest(
            @NotBlank @Size(max = 40) String supplierCode,
            @NotBlank @Size(max = 200) String supplierName,
            @Size(max = 200) String contactName,
            @NotBlank @Email @Size(max = 320) String contactEmail,
            @Size(max = 40) String contactPhone,
            @NotNull @PositiveOrZero Integer leadTimeDays,
            @NotBlank @Size(max = 100) String paymentTerms) {
    }

    public record UpdateSupplierRequest(
            @NotBlank @Size(max = 200) String supplierName,
            @Size(max = 200) String contactName,
            @NotBlank @Email @Size(max = 320) String contactEmail,
            @Size(max = 40) String contactPhone,
            @NotNull @PositiveOrZero Integer leadTimeDays,
            @NotBlank @Size(max = 100) String paymentTerms) {
    }

    public record ChangeSupplierStatusRequest(@NotNull SupplierStatus status) {
    }

    public record SupplierProductRequest(
            @NotNull @Positive Long productId,
            @NotBlank @Size(max = 100) String supplierSku,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal unitCost,
            @NotNull @Positive Integer minimumOrderQuantity,
            @NotNull @PositiveOrZero Integer leadTimeDays) {
    }

    public record UpdateSupplierProductRequest(
            @NotBlank @Size(max = 100) String supplierSku,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal unitCost,
            @NotNull @Positive Integer minimumOrderQuantity,
            @NotNull @PositiveOrZero Integer leadTimeDays,
            @NotNull Boolean active) {
    }

    public record CreatePurchaseOrderRequest(
            @NotNull @Positive Long supplierId,
            @NotNull @Positive Long warehouseId,
            @NotNull @FutureOrPresent LocalDate expectedDate,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal taxAmount,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal shippingAmount,
            @NotEmpty @Size(max = 500) List<@Valid PurchaseOrderItemRequest> items) {
    }

    public record PurchaseOrderItemRequest(
            @NotNull @Positive Long supplierProductId,
            @NotNull @Positive Integer quantity) {
    }

    public record ApprovePurchaseOrderRequest(
            @NotBlank @Size(max = 200) String approvedBy) {
    }

    public record ReceiveGoodsRequest(
            @NotNull @Positive Long purchaseOrderItemId,
            @NotNull @Positive Integer receivedQuantity,
            @NotNull @PositiveOrZero Integer rejectedQuantity,
            @NotBlank @Size(max = 100) String receiptReference,
            @NotBlank @Size(max = 200) String receivedBy,
            @NotNull Instant receivedAt,
            @Size(max = 1000) String notes) {
    }

    public record SupplierResponse(
            Long supplierId,
            String supplierCode,
            String supplierName,
            String contactName,
            String contactEmail,
            String contactPhone,
            SupplierStatus supplierStatus,
            Integer leadTimeDays,
            String paymentTerms,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record SupplierProductResponse(
            Long supplierProductId,
            Long supplierId,
            Long productId,
            String supplierSku,
            BigDecimal unitCost,
            Integer minimumOrderQuantity,
            Integer leadTimeDays,
            Boolean active,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record PurchaseOrderResponse(
            Long purchaseOrderId,
            String poNumber,
            Long supplierId,
            Long warehouseId,
            PurchaseOrderStatus purchaseOrderStatus,
            LocalDate orderDate,
            LocalDate expectedDate,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal shippingAmount,
            BigDecimal totalAmount,
            String createdBy,
            String approvedBy,
            Instant approvedAt,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record PurchaseOrderItemResponse(
            Long purchaseOrderItemId,
            Long purchaseOrderId,
            Long supplierProductId,
            Long productId,
            Integer orderedQuantity,
            Integer receivedQuantity,
            BigDecimal unitCost,
            BigDecimal lineTotal) {
    }

    public record GoodsReceiptResponse(
            Long goodsReceiptId,
            Long purchaseOrderId,
            Long purchaseOrderItemId,
            Long productId,
            Integer receivedQuantity,
            Integer rejectedQuantity,
            String receiptReference,
            String receivedBy,
            Instant receivedAt,
            String notes) {
    }

    public record PurchaseOrderDetailResponse(
            PurchaseOrderResponse purchaseOrder,
            List<PurchaseOrderItemResponse> items,
            List<GoodsReceiptResponse> receipts) {
    }
}
