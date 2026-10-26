package com.ecommerce.fulfillment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "shipments", schema = "ecomm")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipment_seq")
    @SequenceGenerator(name = "shipment_seq", sequenceName = "ecomm.shipment_seq", allocationSize = 1)
    @Column(name = "shipment_id")
    private Long shipmentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "carrier_code", nullable = false, length = 40)
    private String carrierCode;

    @Column(name = "service_level", nullable = false, length = 50)
    private String serviceLevel;

    @Column(name = "tracking_number", unique = true, length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false, length = 30)
    private ShipmentStatus shipmentStatus;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Shipment() {
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public ShipmentStatus getShipmentStatus() {
        return shipmentStatus;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public Instant getEstimatedDeliveryAt() {
        return estimatedDeliveryAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
