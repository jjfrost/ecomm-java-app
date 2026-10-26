package com.ecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "order_status_history", schema = "ecomm")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_status_history_generator")
    @SequenceGenerator(
            name = "order_status_history_generator",
            sequenceName = "ecomm.order_status_history_seq",
            allocationSize = 1)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private OrderStatus newStatus;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    protected OrderStatusHistory() {
    }

    public Long getHistoryId() { return historyId; }
    public Long getOrderId() { return orderId; }
    public OrderStatus getPreviousStatus() { return previousStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
    public Instant getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }
    public String getNotes() { return notes; }
}
