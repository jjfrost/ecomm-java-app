package com.ecommerce.supplier.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.supplier.dto.SupplierDtos.ApprovePurchaseOrderRequest;
import com.ecommerce.supplier.dto.SupplierDtos.CreatePurchaseOrderRequest;
import com.ecommerce.supplier.dto.SupplierDtos.GoodsReceiptResponse;
import com.ecommerce.supplier.dto.SupplierDtos.PurchaseOrderDetailResponse;
import com.ecommerce.supplier.dto.SupplierDtos.PurchaseOrderResponse;
import com.ecommerce.supplier.dto.SupplierDtos.ReceiveGoodsRequest;
import com.ecommerce.supplier.model.PurchaseOrderStatus;
import com.ecommerce.supplier.service.ProcurementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/purchase-orders")
@PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
public class ProcurementController {
    private final ProcurementService procurementService;

    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    @GetMapping("/{purchaseOrderId}")
    public PurchaseOrderDetailResponse getPurchaseOrder(
            @PathVariable @Positive Long purchaseOrderId) {
        return procurementService.getPurchaseOrder(purchaseOrderId);
    }

    @GetMapping
    public PageResponse<PurchaseOrderResponse> findPurchaseOrders(
            @RequestParam(required = false) @Positive Long supplierId,
            @RequestParam(required = false) @Positive Long warehouseId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @PageableDefault(size = 25, sort = "createdAt") Pageable pageable) {
        return procurementService.findPurchaseOrders(supplierId, warehouseId, status, pageable);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDetailResponse> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(procurementService.createPurchaseOrder(request));
    }

    @PostMapping("/{purchaseOrderId}/submit")
    public PurchaseOrderResponse submit(@PathVariable @Positive Long purchaseOrderId) {
        return procurementService.submit(purchaseOrderId);
    }

    @PostMapping("/{purchaseOrderId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public PurchaseOrderResponse approve(
            @PathVariable @Positive Long purchaseOrderId,
            @Valid @RequestBody ApprovePurchaseOrderRequest request) {
        return procurementService.approve(purchaseOrderId, request);
    }

    @PostMapping("/{purchaseOrderId}/receipts")
    public ResponseEntity<GoodsReceiptResponse> receiveGoods(
            @PathVariable @Positive Long purchaseOrderId,
            @Valid @RequestBody ReceiveGoodsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(procurementService.receiveGoods(purchaseOrderId, request));
    }

    @PostMapping("/{purchaseOrderId}/cancel")
    public PurchaseOrderResponse cancel(
            @PathVariable @Positive Long purchaseOrderId,
            @RequestParam @NotBlank @Size(max = 500) String reason) {
        return procurementService.cancel(purchaseOrderId, reason);
    }

    @PostMapping("/{purchaseOrderId}/close")
    public PurchaseOrderResponse close(@PathVariable @Positive Long purchaseOrderId) {
        return procurementService.close(purchaseOrderId);
    }
}
