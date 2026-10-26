package com.ecommerce.fulfillment.dto;

import com.ecommerce.fulfillment.model.CarrierEventType;
import com.ecommerce.fulfillment.model.ShipmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class FulfillmentDtos {
    private FulfillmentDtos() {
    }

    public record CreateShipmentRequest(
            @NotNull @Positive Long orderId,
            @NotNull @Positive Long warehouseId,
            @NotBlank @Size(max = 40) String carrierCode,
            @NotBlank @Size(max = 50) String serviceLevel,
            @Size(max = 100) String trackingNumber,
            @Future Instant estimatedDeliveryAt,
            @NotEmpty @Size(max = 200) List<@Valid ShipmentItemRequest> items) {
    }

    public record ShipmentItemRequest(
            @NotNull @Positive Long orderItemId,
            @NotNull @Positive Long productId,
            @NotNull @Positive Integer quantity) {
    }

    public record AssignTrackingRequest(
            @NotBlank @Size(max = 100) String trackingNumber,
            @Future Instant estimatedDeliveryAt) {
    }

    public record DispatchShipmentRequest(
            @NotNull Instant shippedAt) {
    }

    public record CarrierEventRequest(
            @NotNull CarrierEventType eventCode,
            @NotBlank @Size(max = 500) String eventDescription,
            @Size(max = 200) String eventLocation,
            @NotNull Instant eventTime) {
    }

    public record ShipmentResponse(
            Long shipmentId,
            Long orderId,
            Long warehouseId,
            String carrierCode,
            String serviceLevel,
            String trackingNumber,
            ShipmentStatus shipmentStatus,
            Instant shippedAt,
            Instant estimatedDeliveryAt,
            Instant deliveredAt,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ShipmentItemResponse(
            Long shipmentItemId,
            Long shipmentId,
            Long orderItemId,
            Long productId,
            Integer quantity) {
    }

    public record CarrierEventResponse(
            Long carrierEventId,
            Long shipmentId,
            CarrierEventType eventCode,
            String eventDescription,
            String eventLocation,
            Instant eventTime,
            Instant receivedAt) {
    }

    public record ShipmentDetailResponse(
            ShipmentResponse shipment,
            List<ShipmentItemResponse> items,
            List<CarrierEventResponse> events) {
    }
}
