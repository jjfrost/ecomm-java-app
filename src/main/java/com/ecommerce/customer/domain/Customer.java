package com.ecommerce.customer.domain;

import com.ecommerce.common.persistence.OracleYesNoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
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
@Table(name = "customers", schema = "ecomm")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq_generator")
    @SequenceGenerator(
            name = "customer_seq_generator",
            sequenceName = "customer_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Embedded
    private HomeAddress homeAddress;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_active", nullable = false, length = 1)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Customer() {
    }

    public Customer(String name, String email, HomeAddress homeAddress) {
        this.name = normalize(name);
        this.email = normalizeEmail(email);
        this.homeAddress = homeAddress;
        this.active = true;
    }

    public void updateProfile(String name, String email, HomeAddress homeAddress) {
        this.name = normalize(name);
        this.email = normalizeEmail(email);
        this.homeAddress = homeAddress;
    }

    public void deactivate() {
        active = false;
    }

    @PrePersist
    void initializeAuditFields() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    void updateAuditFields() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public HomeAddress getHomeAddress() {
        return homeAddress;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
