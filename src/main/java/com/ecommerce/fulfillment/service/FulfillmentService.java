package com.ecommerce.fulfillment.service;

import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.AssignTrackingRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CarrierEventRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CarrierEventResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CreateShipmentRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.DispatchShipmentRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentDetailResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentItemResponse;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentResponse;
import com.ecommerce.fulfillment.gateway.FulfillmentPackageGateway;
import com.ecommerce.fulfillment.mapper.FulfillmentMapper;
import com.ecommerce.fulfillment.model.CarrierEvent;
import com.ecommerce.fulfillment.model.CarrierEventType;
import com.ecommerce.fulfillment.model.Shipment;
import com.ecommerce.fulfillment.model.ShipmentStatus;
import com.ecommerce.fulfillment.repository.CarrierEventRepository;
import com.ecommerce.fulfillment.repository.ShipmentItemRepository;
import com.ecommerce.fulfillment.repository.ShipmentRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FulfillmentService {
    private static final int MAX_PAGE_SIZE = 200;
    private static final Duration MAX_EVENT_CLOCK_SKEW = Duration.ofMinutes(5);

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final CarrierEventRepository carrierEventRepository;
    private final FulfillmentPackageGateway fulfillmentGateway;
    private final FulfillmentMapper fulfillmentMapper;
    private final AuditService auditService;

    public FulfillmentService(
            ShipmentRepository shipmentRepository,
            ShipmentItemRepository shipmentItemRepository,
            CarrierEventRepository carrierEventRepository,
            FulfillmentPackageGateway fulfillmentGateway,
            FulfillmentMapper fulfillmentMapper,
            AuditService auditService) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentItemRepository = shipmentItemRepository;
        this.carrierEventRepository = carrierEventRepository;
        this.fulfillmentGateway = fulfillmentGateway;
        this.fulfillmentMapper = fulfillmentMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public ShipmentDetailResponse getShipment(Long shipmentId) {
        Shipment shipment = requireShipment(shipmentId);
        List<ShipmentItemResponse> items = shipmentItemRepository
                .findByShipmentIdOrderByShipmentItemId(shipmentId)
                .stream()
                .map(fulfillmentMapper::toResponse)
                .toList();
        List<CarrierEventResponse> events = carrierEventRepository
                .findByShipmentIdOrderByEventTimeAsc(shipmentId)
                .stream()
                .map(fulfillmentMapper::toResponse)
                .toList();
        return new ShipmentDetailResponse(fulfillmentMapper.toResponse(shipment), items, events);
    }

    @Transactional(readOnly = true)
    public ShipmentDetailResponse track(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment tracking number " + trackingNumber + " was not found"));
        return getShipment(shipment.getShipmentId());
    }

    @Transactional(readOnly = true)
    public PageResponse<ShipmentResponse> findShipments(
            Long orderId,
            ShipmentStatus status,
            String carrierCode,
            Pageable pageable) {
        validatePage(pageable);
        return PageResponse.from(
                shipmentRepository.search(
                        orderId,
                        status,
                        normalize(carrierCode),
                        pageable),
                fulfillmentMapper::toResponse);
    }

    @Transactional
    public ShipmentDetailResponse createShipment(CreateShipmentRequest request) {
        validateShipmentItems(request);
        if (request.trackingNumber() != null
                && shipmentRepository.existsByTrackingNumber(request.trackingNumber())) {
            throw new BusinessRuleException(
                    "TRACKING_NUMBER_EXISTS",
                    "Tracking number is already assigned to another shipment");
        }
        List<Long> orderItemIds = request.items().stream()
                .map(item -> item.orderItemId())
                .toList();
        if (shipmentItemRepository.existsByOrderItemIdIn(orderItemIds)) {
            throw new BusinessRuleException(
                    "ORDER_ITEM_ALREADY_SHIPPED",
                    "At least one order item is already assigned to a shipment");
        }
        Long shipmentId = fulfillmentGateway.createShipment(request);
        request.items().forEach(item -> fulfillmentGateway.addShipmentItem(shipmentId, item));
        audit(
                "SHIPMENT_CREATED",
                shipmentId,
                "orderId=" + request.orderId() + ",itemCount=" + request.items().size());
        return getShipment(shipmentId);
    }

    @Transactional
    public ShipmentResponse assignTracking(Long shipmentId, AssignTrackingRequest request) {
        Shipment shipment = lockShipment(shipmentId);
        requireState(shipment, Set.of(ShipmentStatus.PENDING, ShipmentStatus.READY), "assign tracking");
        if (shipmentRepository.existsByTrackingNumber(request.trackingNumber())
                && !request.trackingNumber().equals(shipment.getTrackingNumber())) {
            throw new BusinessRuleException(
                    "TRACKING_NUMBER_EXISTS",
                    "Tracking number is already assigned to another shipment");
        }
        fulfillmentGateway.assignTracking(shipmentId, request);
        audit("SHIPMENT_TRACKING_ASSIGNED", shipmentId, "trackingNumber=" + request.trackingNumber());
        return fulfillmentMapper.toResponse(requireShipment(shipmentId));
    }

    @Transactional
    public ShipmentResponse markReady(Long shipmentId) {
        Shipment shipment = lockShipment(shipmentId);
        requireState(shipment, Set.of(ShipmentStatus.PENDING), "mark ready");
        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isBlank()) {
            throw new BusinessRuleException(
                    "TRACKING_NUMBER_REQUIRED",
                    "Shipment requires a tracking number before it can be ready");
        }
        fulfillmentGateway.markReady(shipmentId);
        audit("SHIPMENT_READY", shipmentId, "orderId=" + shipment.getOrderId());
        return fulfillmentMapper.toResponse(requireShipment(shipmentId));
    }

    @Transactional
    public ShipmentResponse dispatch(Long shipmentId, DispatchShipmentRequest request) {
        Shipment shipment = lockShipment(shipmentId);
        requireState(shipment, Set.of(ShipmentStatus.READY), "dispatch");
        if (request.shippedAt().isAfter(Instant.now().plus(MAX_EVENT_CLOCK_SKEW))) {
            throw new BusinessRuleException(
                    "INVALID_SHIPPED_TIME",
                    "Shipment dispatch time cannot be in the future");
        }
        fulfillmentGateway.dispatch(shipmentId, request.shippedAt());
        audit("SHIPMENT_DISPATCHED", shipmentId, "shippedAt=" + request.shippedAt());
        return fulfillmentMapper.toResponse(requireShipment(shipmentId));
    }

    @Transactional
    public CarrierEventResponse recordCarrierEvent(
            Long shipmentId,
            CarrierEventRequest request) {
        Shipment shipment = lockShipment(shipmentId);
        validateCarrierEvent(shipment, request);
        Long eventId = fulfillmentGateway.recordCarrierEvent(shipmentId, request);
        CarrierEvent event = carrierEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier event", eventId));
        audit(
                "CARRIER_EVENT_RECORDED",
                shipmentId,
                "eventId=" + eventId + ",eventCode=" + request.eventCode());
        return fulfillmentMapper.toResponse(event);
    }

    @Transactional
    public ShipmentResponse cancel(Long shipmentId, String reason) {
        Shipment shipment = lockShipment(shipmentId);
        requireState(
                shipment,
                Set.of(ShipmentStatus.PENDING, ShipmentStatus.READY),
                "cancel");
        String normalizedReason = normalize(reason);
        if (normalizedReason == null) {
            throw new BusinessRuleException(
                    "CANCELLATION_REASON_REQUIRED",
                    "Shipment cancellation requires a reason");
        }
        fulfillmentGateway.cancel(shipmentId, normalizedReason);
        audit("SHIPMENT_CANCELLED", shipmentId, "reason=" + normalizedReason);
        return fulfillmentMapper.toResponse(requireShipment(shipmentId));
    }

    private void validateShipmentItems(CreateShipmentRequest request) {
        Set<Long> uniqueItems = new HashSet<>();
        for (var item : request.items()) {
            if (!uniqueItems.add(item.orderItemId())) {
                throw new BusinessRuleException(
                        "DUPLICATE_SHIPMENT_ITEM",
                        "Each order item can appear only once in a shipment");
            }
        }
    }

    private void validateCarrierEvent(Shipment shipment, CarrierEventRequest request) {
        if (shipment.getShipmentStatus().isTerminal()) {
            throw new BusinessRuleException(
                    "SHIPMENT_TERMINAL",
                    "Carrier events cannot be added to a terminal shipment");
        }
        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isBlank()) {
            throw new BusinessRuleException(
                    "TRACKING_NUMBER_REQUIRED",
                    "Carrier events require an assigned tracking number");
        }
        if (request.eventTime().isAfter(Instant.now().plus(MAX_EVENT_CLOCK_SKEW))) {
            throw new BusinessRuleException(
                    "CARRIER_EVENT_IN_FUTURE",
                    "Carrier event time cannot be more than five minutes in the future");
        }
        carrierEventRepository.findFirstByShipmentIdOrderByEventTimeDesc(shipment.getShipmentId())
                .ifPresent(previous -> {
                    if (request.eventTime().isBefore(previous.getEventTime())) {
                        throw new BusinessRuleException(
                                "CARRIER_EVENT_OUT_OF_ORDER",
                                "Carrier event time cannot precede the latest recorded event");
                    }
                });
        if (request.eventCode() != CarrierEventType.LABEL_CREATED
                && shipment.getShipmentStatus() != ShipmentStatus.SHIPPED
                && shipment.getShipmentStatus() != ShipmentStatus.IN_TRANSIT
                && shipment.getShipmentStatus() != ShipmentStatus.EXCEPTION) {
            throw new BusinessRuleException(
                    "SHIPMENT_NOT_DISPATCHED",
                    "Movement events require a dispatched shipment");
        }
    }

    private Shipment lockShipment(Long shipmentId) {
        return shipmentRepository.findByIdForUpdate(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));
    }

    private Shipment requireShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId));
    }

    private void requireState(
            Shipment shipment,
            Set<ShipmentStatus> states,
            String operation) {
        if (!states.contains(shipment.getShipmentStatus())) {
            throw new BusinessRuleException(
                    "INVALID_SHIPMENT_STATE",
                    "Cannot " + operation + " shipment in " + shipment.getShipmentStatus() + " state");
        }
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Shipment page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void audit(String eventType, Long shipmentId, String payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null ? "system" : authentication.getName();
        auditService.record(new WriteAuditEventRequest(
                eventType,
                "Shipment",
                shipmentId.toString(),
                actor,
                MDC.get("correlationId"),
                payload,
                null));
    }
}
