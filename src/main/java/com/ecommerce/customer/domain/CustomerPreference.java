package com.ecommerce.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "customer_preferences", schema = "ecomm")
public class CustomerPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_preference_seq_generator")
    @SequenceGenerator(
            name = "customer_preference_seq_generator",
            sequenceName = "customer_preference_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "preference_id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "preference_key", nullable = false, length = 80)
    private String key;

    @Column(name = "preference_value", nullable = false, length = 500)
    private String value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerPreference() {
    }

    public CustomerPreference(Long customerId, String key, String value) {
        this.customerId = customerId;
        this.key = normalizeKey(key);
        this.value = value.trim();
    }

    public void changeValue(String value) {
        this.value = value.trim();
    }

    @PrePersist
    void initializeAuditFields() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateAuditFields() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeKey(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
