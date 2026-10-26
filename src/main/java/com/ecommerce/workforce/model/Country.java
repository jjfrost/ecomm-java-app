package com.ecommerce.workforce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "countries", schema = "ecomm")
public class Country {
    @Id
    @Column(name = "country_id", length = 2)
    private String countryId;

    @Column(name = "country_name", nullable = false, length = 100)
    private String countryName;

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }
}
