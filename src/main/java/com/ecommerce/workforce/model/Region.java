package com.ecommerce.workforce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "regions", schema = "ecomm")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_seq")
    @SequenceGenerator(name = "region_seq", sequenceName = "ecomm.region_seq", allocationSize = 1)
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
