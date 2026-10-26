package com.ecommerce.inventory.domain;

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
@Table(name = "warehouses", schema = "ecomm")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_generator")
    @SequenceGenerator(
            name = "warehouse_generator",
            sequenceName = "ecomm.warehouse_seq",
            allocationSize = 1)
    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "warehouse_code", nullable = false, unique = true, length = 30)
    private String warehouseCode;

    @Column(name = "warehouse_name", nullable = false, length = 100)
    private String name;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WarehouseStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Warehouse() {
    }

    public static Warehouse create(
            String warehouseCode,
            String name,
            String location,
            Integer capacity,
            WarehouseStatus status) {
        Warehouse warehouse = new Warehouse();
        warehouse.applyChanges(warehouseCode, name, location, capacity, status);
        return warehouse;
    }

    public void applyChanges(
            String warehouseCode,
            String name,
            String location,
            Integer capacity,
            WarehouseStatus status) {
        this.warehouseCode = warehouseCode;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.status = status;
    }

    public boolean isOperational() {
        return status == WarehouseStatus.ACTIVE;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = WarehouseStatus.ACTIVE;
        }
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public WarehouseStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
