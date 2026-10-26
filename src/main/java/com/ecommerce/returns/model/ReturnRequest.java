package com.ecommerce.returns.model;

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
@Table(name = "return_requests", schema = "ecomm")
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "return_request_seq")
    @SequenceGenerator(
            name = "return_request_seq",
            sequenceName = "ecomm.return_request_seq",
            allocationSize = 1)
    @Column(name = "return_request_id")
    private Long returnRequestId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_reason", nullable = false, length = 40)
    private ReturnReason returnReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", nullable = false, length = 30)
    private ReturnStatus returnStatus;

    @Column(name = "authorization_code", unique = true, length = 100)
    private String authorizationCode;

    @Column(name = "customer_notes", length = 2000)
    private String customerNotes;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ReturnRequest() {
    }

    public Long getReturnRequestId() {
        return returnRequestId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public ReturnReason getReturnReason() {
        return returnReason;
    }

    public ReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getAuthorizedAt() {
        return authorizedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
