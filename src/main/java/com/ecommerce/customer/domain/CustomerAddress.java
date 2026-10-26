package com.ecommerce.customer.domain;

import com.ecommerce.common.persistence.OracleYesNoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.Locale;

@Entity
@Table(name = "customer_addresses", schema = "ecomm")
public class CustomerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_address_seq_generator")
    @SequenceGenerator(
            name = "customer_address_seq_generator",
            sequenceName = "customer_address_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "address_id")
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType type;

    @Column(name = "label", nullable = false, length = 80)
    private String label;

    @Column(name = "street", nullable = false, length = 250)
    private String street;

    @Column(name = "city", nullable = false, length = 120)
    private String city;

    @Column(name = "state", length = 120)
    private String state;

    @Column(name = "zip", nullable = false, length = 20)
    private String zip;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Convert(converter = OracleYesNoConverter.class)
    @Column(name = "is_default", nullable = false, length = 1)
    private Boolean defaultAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerAddress() {
    }

    public CustomerAddress(
            Long customerId,
            AddressType type,
            String label,
            String street,
            String city,
            String state,
            String zip,
            String countryCode,
            boolean defaultAddress) {
        this.customerId = customerId;
        update(type, label, street, city, state, zip, countryCode);
        this.defaultAddress = defaultAddress;
    }

    public void update(
            AddressType type,
            String label,
            String street,
            String city,
            String state,
            String zip,
            String countryCode) {
        this.type = type;
        this.label = normalize(label);
        this.street = normalize(street);
        this.city = normalize(city);
        this.state = normalizeNullable(state);
        this.zip = normalize(zip);
        this.countryCode = countryCode == null
                ? null
                : countryCode.trim().toUpperCase(Locale.ROOT);
    }

    public void makeDefault() {
        defaultAddress = true;
    }

    public void clearDefault() {
        defaultAddress = false;
    }

    @PrePersist
    void initializeAuditFields() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (defaultAddress == null) {
            defaultAddress = false;
        }
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

    public AddressType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public boolean isDefaultAddress() {
        return Boolean.TRUE.equals(defaultAddress);
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

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
