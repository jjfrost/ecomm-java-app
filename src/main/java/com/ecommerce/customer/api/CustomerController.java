package com.ecommerce.customer.api;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.customer.api.dto.CustomerAddressRequest;
import com.ecommerce.customer.api.dto.CustomerAddressResponse;
import com.ecommerce.customer.api.dto.CustomerCreateRequest;
import com.ecommerce.customer.api.dto.CustomerDetailResponse;
import com.ecommerce.customer.api.dto.CustomerPreferenceRequest;
import com.ecommerce.customer.api.dto.CustomerPreferenceResponse;
import com.ecommerce.customer.api.dto.CustomerSummaryResponse;
import com.ecommerce.customer.api.dto.CustomerUpdateRequest;
import com.ecommerce.customer.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerDetailResponse> create(
            @Valid @RequestBody CustomerCreateRequest request) {
        CustomerDetailResponse response = customerService.create(request);
        return ResponseEntity
                .created(URI.create("/api/customers/" + response.id()))
                .body(response);
    }

    @GetMapping
    public PageResponse<CustomerSummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return customerService.search(q, active, pageable);
    }

    @GetMapping("/{customerId}")
    public CustomerDetailResponse get(
            @PathVariable @Positive Long customerId) {
        return customerService.get(customerId);
    }

    @PutMapping("/{customerId}")
    public CustomerDetailResponse update(
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.update(customerId, request);
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable @Positive Long customerId) {
        customerService.deactivate(customerId);
    }

    @GetMapping("/{customerId}/addresses")
    public List<CustomerAddressResponse> listAddresses(
            @PathVariable @Positive Long customerId) {
        return customerService.listAddresses(customerId);
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<CustomerAddressResponse> addAddress(
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody CustomerAddressRequest request) {
        CustomerAddressResponse response = customerService.addAddress(customerId, request);
        return ResponseEntity
                .created(URI.create(
                        "/api/customers/" + customerId + "/addresses/" + response.id()))
                .body(response);
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public CustomerAddressResponse updateAddress(
            @PathVariable @Positive Long customerId,
            @PathVariable @Positive Long addressId,
            @Valid @RequestBody CustomerAddressRequest request) {
        return customerService.updateAddress(customerId, addressId, request);
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @PathVariable @Positive Long customerId,
            @PathVariable @Positive Long addressId) {
        customerService.deleteAddress(customerId, addressId);
    }

    @GetMapping("/{customerId}/preferences")
    public List<CustomerPreferenceResponse> listPreferences(
            @PathVariable @Positive Long customerId) {
        return customerService.listPreferences(customerId);
    }

    @PutMapping("/{customerId}/preferences/{key}")
    public CustomerPreferenceResponse setPreference(
            @PathVariable @Positive Long customerId,
            @PathVariable String key,
            @Valid @RequestBody CustomerPreferenceRequest request) {
        return customerService.setPreference(customerId, key, request);
    }

    @DeleteMapping("/{customerId}/preferences/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePreference(
            @PathVariable @Positive Long customerId,
            @PathVariable String key) {
        customerService.deletePreference(customerId, key);
    }
}
