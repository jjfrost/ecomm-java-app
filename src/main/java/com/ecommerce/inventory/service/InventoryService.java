package com.ecommerce.inventory.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.domain.Inventory;
import com.ecommerce.inventory.domain.InventoryReservation;
import com.ecommerce.inventory.domain.InventoryReservationStatus;
import com.ecommerce.inventory.dto.InventoryAuditResponse;
import com.ecommerce.inventory.dto.InventoryReservationResponse;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.StockAdjustmentRequest;
import com.ecommerce.inventory.dto.StockMovementResponse;
import com.ecommerce.inventory.dto.StockReceiptRequest;
import com.ecommerce.inventory.dto.StockReleaseRequest;
import com.ecommerce.inventory.dto.StockReservationRequest;
import com.ecommerce.inventory.repository.InventoryAuditLogRepository;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.InventoryReservationRepository;
import com.ecommerce.inventory.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class InventoryService {
    private static final Duration DEFAULT_RESERVATION_DURATION = Duration.ofMinutes(30);

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final StockMovementRepository movementRepository;
    private final InventoryAuditLogRepository auditRepository;
    private final InventoryCommandGateway commandGateway;
    private final InventoryMapper inventoryMapper;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryReservationRepository reservationRepository,
            StockMovementRepository movementRepository,
            InventoryAuditLogRepository auditRepository,
            InventoryCommandGateway commandGateway,
            InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.movementRepository = movementRepository;
        this.auditRepository = auditRepository;
        this.commandGateway = commandGateway;
        this.inventoryMapper = inventoryMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> list(
            Long productId,
            String location,
            boolean reorderOnly,
            Pageable pageable) {
        Page<Inventory> inventory;
        if (reorderOnly) {
            inventory = inventoryRepository.findReorderCandidates(pageable);
        } else if (productId != null && location != null && !location.isBlank()) {
            List<Inventory> content = inventoryRepository
                    .findByProductIdAndLocationIgnoreCase(productId, location.trim())
                    .stream()
                    .toList();
            inventory = new PageImpl<>(content, pageable, content.size());
        } else if (productId != null) {
            inventory = inventoryRepository.findByProductId(productId, pageable);
        } else if (location != null && !location.isBlank()) {
            inventory = inventoryRepository.findByLocationContainingIgnoreCase(location.trim(), pageable);
        } else {
            inventory = inventoryRepository.findAll(pageable);
        }
        return PageResponse.from(inventory, inventoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse get(Long inventoryId) {
        return inventoryMapper.toResponse(requireInventory(inventoryId));
    }

    @Transactional(readOnly = true)
    public InventoryResponse getByProductAndLocation(Long productId, String location) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndLocationIgnoreCase(productId, location.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory was not found for product " + productId + " at location " + location));
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public InventoryResponse receive(Long inventoryId, StockReceiptRequest request) {
        Inventory inventory = requireLockedInventory(inventoryId);
        inventory.assertCanReceive(request.quantity());
        commandGateway.receiveStock(
                inventory,
                request.quantity(),
                request.reference().trim(),
                request.performedBy().trim());
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public InventoryResponse adjust(Long inventoryId, StockAdjustmentRequest request) {
        if (request.quantityDelta().signum() == 0) {
            throw new BusinessRuleException(
                    "ZERO_STOCK_ADJUSTMENT",
                    "Stock adjustment quantity cannot be zero");
        }
        Inventory inventory = requireLockedInventory(inventoryId);
        inventory.assertCanAdjust(request.quantityDelta());
        commandGateway.adjustStock(
                inventory,
                request.quantityDelta(),
                request.reason().trim(),
                request.performedBy().trim());
        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public InventoryReservationResponse reserve(
            Long inventoryId,
            StockReservationRequest request) {
        Inventory inventory = requireLockedInventory(inventoryId);
        return reserveLockedInventory(
                inventory,
                request.orderId(),
                request.quantity(),
                request.expiresAt(),
                request.performedBy());
    }

    @Transactional
    public InventoryReservationResponse reserveProduct(
            Long productId,
            Long orderId,
            BigDecimal quantity,
            Instant expiresAt,
            String performedBy) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessRuleException(
                    "INVALID_RESERVATION_QUANTITY",
                    "Reservation quantity must be greater than zero");
        }
        List<Inventory> candidates = inventoryRepository.findReservableInventory(
                productId,
                quantity,
                PageRequest.of(0, 1));
        if (candidates.isEmpty()) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_AVAILABLE_STOCK",
                    "No inventory location can satisfy the requested quantity for product " + productId);
        }
        return reserveLockedInventory(
                candidates.get(0),
                orderId,
                quantity,
                expiresAt,
                performedBy);
    }

    @Transactional
    public InventoryReservationResponse release(
            Long reservationId,
            StockReleaseRequest request) {
        InventoryReservation reservation = requireLockedReservation(reservationId);
        releaseLockedReservation(reservation, request.reason(), request.performedBy());
        return inventoryMapper.toResponse(reservation);
    }

    @Transactional
    public void releaseForOrder(Long orderId, String reason, String performedBy) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderIdAndStatusIn(
                orderId,
                List.of(InventoryReservationStatus.ACTIVE));
        for (InventoryReservation reservation : reservations) {
            InventoryReservation locked = requireLockedReservation(reservation.getReservationId());
            releaseLockedReservation(locked, reason, performedBy);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryReservationResponse> reservations(
            Long inventoryId,
            Pageable pageable) {
        requireInventory(inventoryId);
        return PageResponse.from(
                reservationRepository.findByInventoryId(inventoryId, pageable),
                inventoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> movements(Long inventoryId, Pageable pageable) {
        requireInventory(inventoryId);
        return PageResponse.from(
                movementRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId, pageable),
                inventoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryAuditResponse> audits(Long inventoryId, Pageable pageable) {
        requireInventory(inventoryId);
        return PageResponse.from(
                auditRepository.findByInventoryIdOrderByChangedAtDesc(inventoryId, pageable),
                inventoryMapper::toResponse);
    }

    private InventoryReservationResponse reserveLockedInventory(
            Inventory inventory,
            Long orderId,
            BigDecimal quantity,
            Instant requestedExpiry,
            String performedBy) {
        inventory.assertCanReserve(quantity);
        if (reservationRepository.existsByInventoryIdAndOrderIdAndStatus(
                inventory.getInventoryId(),
                orderId,
                InventoryReservationStatus.ACTIVE)) {
            throw new BusinessRuleException(
                    "DUPLICATE_ACTIVE_RESERVATION",
                    "An active reservation already exists for this inventory and order");
        }
        Instant expiresAt = requestedExpiry == null
                ? Instant.now().plus(DEFAULT_RESERVATION_DURATION)
                : requestedExpiry;
        if (!expiresAt.isAfter(Instant.now())) {
            throw new BusinessRuleException(
                    "INVALID_RESERVATION_EXPIRY",
                    "Reservation expiry must be in the future");
        }
        Long reservationId = commandGateway.reserveStock(
                inventory,
                orderId,
                quantity,
                expiresAt,
                performedBy.trim());
        InventoryReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryReservation", reservationId));
        return inventoryMapper.toResponse(reservation);
    }

    private void releaseLockedReservation(
            InventoryReservation reservation,
            String reason,
            String performedBy) {
        if (reservation.getStatus() != InventoryReservationStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "RESERVATION_NOT_ACTIVE",
                    "Only an active reservation can be released");
        }
        commandGateway.releaseStock(
                reservation,
                reason.trim(),
                performedBy.trim());
    }

    private Inventory requireInventory(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", inventoryId));
    }

    private Inventory requireLockedInventory(Long inventoryId) {
        return inventoryRepository.findLockedById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", inventoryId));
    }

    private InventoryReservation requireLockedReservation(Long reservationId) {
        return reservationRepository.findLockedById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryReservation", reservationId));
    }
}
