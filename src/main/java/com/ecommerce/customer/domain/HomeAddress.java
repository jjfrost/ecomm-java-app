package com.ecommerce.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class HomeAddress {
    @Column(name = "home_address_street", length = 250)
    private String street;

    @Column(name = "home_address_city", length = 120)
    private String city;

    @Column(name = "home_address_zip", length = 20)
    private String zip;

    protected HomeAddress() {
    }

    public HomeAddress(String street, String city, String zip) {
        this.street = normalize(street);
        this.city = normalize(city);
        this.zip = normalize(zip);
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getZip() {
        return zip;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
