package com.ecommerce.inventory.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.inventory.dto.InventoryAuditResponse;
import com.ecommerce.inventory.dto.InventoryReservationResponse;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.StockAdjustmentRequest;
import com.ecommerce.inventory.dto.StockMovementResponse;
import com.ecommerce.inventory.dto.StockReceiptRequest;
import com.ecommerce.inventory.dto.StockReleaseRequest;
import com.ecommerce.inventory.dto.StockReservationRequest;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public PageResponse<InventoryResponse> list(
            @RequestParam(required = false) @Positive Long productId,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "false") boolean reorderOnly,
            Pageable pageable) {
        return inventoryService.list(productId, location, reorderOnly, pageable);
    }

    @GetMapping("/{inventoryId}")
    public InventoryResponse get(@PathVariable @Positive Long inventoryId) {
        return inventoryService.get(inventoryId);
    }

    @GetMapping("/product/{productId}")
    public InventoryResponse getByProductAndLocation(
            @PathVariable @Positive Long productId,
            @RequestParam @NotBlank String location) {
        return inventoryService.getByProductAndLocation(productId, location);
    }

    @PostMapping("/{inventoryId}/receipts")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public InventoryResponse receive(
            @PathVariable @Positive Long inventoryId,
            @Valid @RequestBody StockReceiptRequest request) {
        return inventoryService.receive(inventoryId, request);
    }

    @PostMapping("/{inventoryId}/adjustments")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public InventoryResponse adjust(
            @PathVariable @Positive Long inventoryId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return inventoryService.adjust(inventoryId, request);
    }

    @PostMapping("/{inventoryId}/reservations")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<InventoryReservationResponse> reserve(
            @PathVariable @Positive Long inventoryId,
            @Valid @RequestBody StockReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.reserve(inventoryId, request));
    }

    @PostMapping("/reservations/{reservationId}/release")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public InventoryReservationResponse release(
            @PathVariable @Positive Long reservationId,
            @Valid @RequestBody StockReleaseRequest request) {
        return inventoryService.release(reservationId, request);
    }

    @GetMapping("/{inventoryId}/reservations")
    public PageResponse<InventoryReservationResponse> reservations(
            @PathVariable @Positive Long inventoryId,
            Pageable pageable) {
        return inventoryService.reservations(inventoryId, pageable);
    }

    @GetMapping("/{inventoryId}/movements")
    public PageResponse<StockMovementResponse> movements(
            @PathVariable @Positive Long inventoryId,
            Pageable pageable) {
        return inventoryService.movements(inventoryId, pageable);
    }

    @GetMapping("/{inventoryId}/audit")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<InventoryAuditResponse> audits(
            @PathVariable @Positive Long inventoryId,
            Pageable pageable) {
        return inventoryService.audits(inventoryId, pageable);
    }
}
