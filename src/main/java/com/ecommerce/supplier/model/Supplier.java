package com.ecommerce.supplier.model;

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
@Table(name = "suppliers", schema = "ecomm")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supplier_seq")
    @SequenceGenerator(name = "supplier_seq", sequenceName = "ecomm.supplier_seq", allocationSize = 1)
    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_code", nullable = false, unique = true, length = 40)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @Column(name = "contact_email", nullable = false, length = 320)
    private String contactEmail;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_status", nullable = false, length = 30)
    private SupplierStatus supplierStatus;

    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays;

    @Column(name = "payment_terms", nullable = false, length = 100)
    private String paymentTerms;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Supplier() {
    }

    public Supplier(
            String supplierCode,
            String supplierName,
            String contactName,
            String contactEmail,
            String contactPhone,
            Integer leadTimeDays,
            String paymentTerms) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.leadTimeDays = leadTimeDays;
        this.paymentTerms = paymentTerms;
        this.supplierStatus = SupplierStatus.ACTIVE;
    }

    public void update(
            String supplierName,
            String contactName,
            String contactEmail,
            String contactPhone,
            Integer leadTimeDays,
            String paymentTerms) {
        this.supplierName = supplierName;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.leadTimeDays = leadTimeDays;
        this.paymentTerms = paymentTerms;
    }

    public void changeStatus(SupplierStatus supplierStatus) {
        this.supplierStatus = supplierStatus;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public SupplierStatus getSupplierStatus() {
        return supplierStatus;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
