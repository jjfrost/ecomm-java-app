package com.ecommerce.fulfillment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "carrier_events", schema = "ecomm")
public class CarrierEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "carrier_event_seq")
    @SequenceGenerator(
            name = "carrier_event_seq",
            sequenceName = "ecomm.carrier_event_seq",
            allocationSize = 1)
    @Column(name = "carrier_event_id")
    private Long carrierEventId;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_code", nullable = false, length = 40)
    private CarrierEventType eventCode;

    @Column(name = "event_description", nullable = false, length = 500)
    private String eventDescription;

    @Column(name = "event_location", length = 200)
    private String eventLocation;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected CarrierEvent() {
    }

    @PrePersist
    void initializeReceivedAt() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
    }

    public Long getCarrierEventId() {
        return carrierEventId;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public CarrierEventType getEventCode() {
        return eventCode;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
