package com.ecommerce.fulfillment.service;

import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CreateShipmentRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentDetailResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentItemRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentItemResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentResponse;
import com.ecommerce.fulfillment.gateway.FulfillmentPackageGateway;
import com.ecommerce.fulfillment.mapper.FulfillmentMapper;
import com.ecommerce.fulfillment.model.Shipment;
import com.ecommerce.fulfillment.model.ShipmentItem;
import com.ecommerce.fulfillment.repository.CarrierEventRepository;
import com.ecommerce.fulfillment.repository.ShipmentItemRepository;
import com.ecommerce.fulfillment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceTest {
    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentItemRepository shipmentItemRepository;

    @Mock
    private CarrierEventRepository carrierEventRepository;

    @Mock
    private FulfillmentPackageGateway fulfillmentGateway;

    @Mock
    private FulfillmentMapper fulfillmentMapper;

    @Mock
    private AuditService auditService;

    private FulfillmentService fulfillmentService;

    @BeforeEach
    void setUp() {
        fulfillmentService = new FulfillmentService(
                shipmentRepository,
                shipmentItemRepository,
                carrierEventRepository,
                fulfillmentGateway,
                fulfillmentMapper,
                auditService);
    }

    @Test
    void createsShipmentItemsThroughGatewayAndReturnsAggregate() {
        ShipmentItemRequest itemRequest = new ShipmentItemRequest(501L, 301L, 2);
        CreateShipmentRequest request = new CreateShipmentRequest(
                41L,
                8L,
                "UPS",
                "GROUND",
                "TRACK-101",
                Instant.parse("2026-10-10T20:00:00Z"),
                List.of(itemRequest));
        Shipment shipment = org.mockito.Mockito.mock(Shipment.class);
        ShipmentItem item = org.mockito.Mockito.mock(ShipmentItem.class);
        ShipmentResponse shipmentResponse = org.mockito.Mockito.mock(ShipmentResponse.class);
        ShipmentItemResponse itemResponse = org.mockito.Mockito.mock(ShipmentItemResponse.class);
        when(shipmentRepository.existsByTrackingNumber("TRACK-101")).thenReturn(false);
        when(shipmentItemRepository.existsByOrderItemIdIn(List.of(501L))).thenReturn(false);
        when(fulfillmentGateway.createShipment(request)).thenReturn(101L);
        when(shipmentRepository.findById(101L)).thenReturn(Optional.of(shipment));
        when(shipmentItemRepository.findByShipmentIdOrderByShipmentItemId(101L))
                .thenReturn(List.of(item));
        when(carrierEventRepository.findByShipmentIdOrderByEventTimeAsc(101L))
                .thenReturn(List.of());
        when(fulfillmentMapper.toResponse(shipment)).thenReturn(shipmentResponse);
        when(fulfillmentMapper.toResponse(item)).thenReturn(itemResponse);

        ShipmentDetailResponse result = fulfillmentService.createShipment(request);

        assertSame(shipmentResponse, result.shipment());
        assertEquals(List.of(itemResponse), result.items());
        verify(fulfillmentGateway).addShipmentItem(101L, itemRequest);
        verify(auditService).record(any(WriteAuditEventRequest.class));
    }
}
