package com.ecommerce.ui.form;

import com.ecommerce.customer.api.dto.CustomerDetailResponse;
import com.ecommerce.customer.api.dto.CustomerUpdateRequest;
import com.ecommerce.customer.api.dto.HomeAddressRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerEditForm {
    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Email
    @Size(max = 320)
    private String email;

    @NotBlank
    @Size(max = 250)
    private String street;

    @NotBlank
    @Size(max = 120)
    private String city;

    @NotBlank
    @Size(max = 20)
    private String zip;

    public static CustomerEditForm from(CustomerDetailResponse customer) {
        CustomerEditForm form = new CustomerEditForm();
        form.name = customer.name();
        form.email = customer.email();
        form.street = customer.homeAddress().street();
        form.city = customer.homeAddress().city();
        form.zip = customer.homeAddress().zip();
        return form;
    }

    public CustomerUpdateRequest toRequest() {
        return new CustomerUpdateRequest(
                name,
                email,
                new HomeAddressRequest(street, city, zip));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
