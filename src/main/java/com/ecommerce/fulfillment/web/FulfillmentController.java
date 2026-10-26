package com.ecommerce.fulfillment.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.AssignTrackingRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CarrierEventRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CarrierEventResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CreateShipmentRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.DispatchShipmentRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentDetailResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentResponse;
import com.ecommerce.fulfillment.model.ShipmentStatus;
import com.ecommerce.fulfillment.service.FulfillmentService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/shipments")
public class FulfillmentController {
    private final FulfillmentService fulfillmentService;

    public FulfillmentController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @GetMapping("/{shipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ShipmentDetailResponse getShipment(@PathVariable @Positive Long shipmentId) {
        return fulfillmentService.getShipment(shipmentId);
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ShipmentDetailResponse track(
            @PathVariable @NotBlank @Size(max = 100) String trackingNumber) {
        return fulfillmentService.track(trackingNumber);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<ShipmentResponse> findShipments(
            @RequestParam(required = false) @Positive Long orderId,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) @Size(max = 40) String carrierCode,
            @PageableDefault(size = 25, sort = "createdAt") Pageable pageable) {
        return fulfillmentService.findShipments(orderId, status, carrierCode, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<ShipmentDetailResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fulfillmentService.createShipment(request));
    }

    @PatchMapping("/{shipmentId}/tracking")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ShipmentResponse assignTracking(
            @PathVariable @Positive Long shipmentId,
            @Valid @RequestBody AssignTrackingRequest request) {
        return fulfillmentService.assignTracking(shipmentId, request);
    }

    @PostMapping("/{shipmentId}/ready")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ShipmentResponse markReady(@PathVariable @Positive Long shipmentId) {
        return fulfillmentService.markReady(shipmentId);
    }

    @PostMapping("/{shipmentId}/dispatch")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ShipmentResponse dispatch(
            @PathVariable @Positive Long shipmentId,
            @Valid @RequestBody DispatchShipmentRequest request) {
        return fulfillmentService.dispatch(shipmentId, request);
    }

    @PostMapping("/{shipmentId}/events")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<CarrierEventResponse> recordCarrierEvent(
            @PathVariable @Positive Long shipmentId,
            @Valid @RequestBody CarrierEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fulfillmentService.recordCarrierEvent(shipmentId, request));
    }

    @PostMapping("/{shipmentId}/cancel")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ShipmentResponse cancel(
            @PathVariable @Positive Long shipmentId,
            @RequestParam @NotBlank @Size(max = 500) String reason) {
        return fulfillmentService.cancel(shipmentId, reason);
    }
}
