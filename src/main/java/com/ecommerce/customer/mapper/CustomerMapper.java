package com.ecommerce.customer.mapper;

import com.ecommerce.customer.api.dto.CustomerAddressResponse;
import com.ecommerce.customer.api.dto.CustomerDetailResponse;
import com.ecommerce.customer.api.dto.CustomerPreferenceResponse;
import com.ecommerce.customer.api.dto.CustomerSummaryResponse;
import com.ecommerce.customer.api.dto.HomeAddressRequest;
import com.ecommerce.customer.api.dto.HomeAddressResponse;
import com.ecommerce.customer.domain.Customer;
import com.ecommerce.customer.domain.CustomerAddress;
import com.ecommerce.customer.domain.CustomerPreference;
import com.ecommerce.customer.domain.HomeAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerMapper {
    public HomeAddress toHomeAddress(HomeAddressRequest request) {
        return new HomeAddress(request.street(), request.city(), request.zip());
    }

    public CustomerSummaryResponse toSummary(Customer customer) {
        return new CustomerSummaryResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                toHomeAddressResponse(customer.getHomeAddress()),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }

    public CustomerDetailResponse toDetail(
            Customer customer,
            List<CustomerAddress> addresses,
            List<CustomerPreference> preferences) {
        return new CustomerDetailResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                toHomeAddressResponse(customer.getHomeAddress()),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                addresses.stream().map(this::toAddressResponse).toList(),
                preferences.stream().map(this::toPreferenceResponse).toList());
    }

    public CustomerAddressResponse toAddressResponse(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(),
                address.getType(),
                address.getLabel(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZip(),
                address.getCountryCode(),
                address.isDefaultAddress(),
                address.getCreatedAt(),
                address.getUpdatedAt());
    }

    public CustomerPreferenceResponse toPreferenceResponse(CustomerPreference preference) {
        return new CustomerPreferenceResponse(
                preference.getKey(),
                preference.getValue(),
                preference.getUpdatedAt());
    }

    private HomeAddressResponse toHomeAddressResponse(HomeAddress address) {
        if (address == null) {
            return null;
        }
        return new HomeAddressResponse(address.getStreet(), address.getCity(), address.getZip());
    }
}
