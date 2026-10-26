package com.ecommerce.fulfillment.mapper;

import com.ecommerce.fulfillment.dto.FulfillmentDtos.CarrierEventResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentItemResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentResponse;
import com.ecommerce.fulfillment.model.CarrierEvent;
import com.ecommerce.fulfillment.model.Shipment;
import com.ecommerce.fulfillment.model.ShipmentItem;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentMapper {
    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getShipmentId(),
                shipment.getOrderId(),
                shipment.getWarehouseId(),
                shipment.getCarrierCode(),
                shipment.getServiceLevel(),
                shipment.getTrackingNumber(),
                shipment.getShipmentStatus(),
                shipment.getShippedAt(),
                shipment.getEstimatedDeliveryAt(),
                shipment.getDeliveredAt(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt());
    }

    public ShipmentItemResponse toResponse(ShipmentItem item) {
        return new ShipmentItemResponse(
                item.getShipmentItemId(),
                item.getShipmentId(),
                item.getOrderItemId(),
                item.getProductId(),
                item.getQuantity());
    }

    public CarrierEventResponse toResponse(CarrierEvent event) {
        return new CarrierEventResponse(
                event.getCarrierEventId(),
                event.getShipmentId(),
                event.getEventCode(),
                event.getEventDescription(),
                event.getEventLocation(),
                event.getEventTime(),
                event.getReceivedAt());
    }
}
