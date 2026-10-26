package com.ecommerce.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_reservations", schema = "ecomm")
public class InventoryReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_reservation_generator")
    @SequenceGenerator(
            name = "inventory_reservation_generator",
            sequenceName = "ecomm.inventory_reservation_seq",
            allocationSize = 1)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryReservationStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    protected InventoryReservation() {
    }

    public boolean isActiveAt(Instant instant) {
        return status == InventoryReservationStatus.ACTIVE
                && (expiresAt == null || expiresAt.isAfter(instant));
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public InventoryReservationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }
}
