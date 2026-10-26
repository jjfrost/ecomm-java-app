package com.ecommerce.customer.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.customer.api.dto.CustomerAddressRequest;
import com.ecommerce.customer.api.dto.CustomerAddressResponse;
import com.ecommerce.customer.api.dto.CustomerCreateRequest;
import com.ecommerce.customer.api.dto.CustomerDetailResponse;
import com.ecommerce.customer.api.dto.CustomerPreferenceRequest;
import com.ecommerce.customer.api.dto.CustomerPreferenceResponse;
import com.ecommerce.customer.api.dto.CustomerSummaryResponse;
import com.ecommerce.customer.api.dto.CustomerUpdateRequest;
import com.ecommerce.customer.domain.Customer;
import com.ecommerce.customer.domain.CustomerAddress;
import com.ecommerce.customer.domain.CustomerPreference;
import com.ecommerce.customer.domain.LoyaltyAccount;
import com.ecommerce.customer.mapper.CustomerMapper;
import com.ecommerce.customer.repository.CustomerAddressRepository;
import com.ecommerce.customer.repository.CustomerPreferenceRepository;
import com.ecommerce.customer.repository.CustomerRepository;
import com.ecommerce.customer.repository.CustomerSpecifications;
import com.ecommerce.customer.repository.LoyaltyAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CustomerService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_ADDRESSES = 10;
    private static final int MAX_PREFERENCES = 25;
    private static final Set<String> CUSTOMER_SORT_FIELDS =
            Set.of("id", "name", "email", "createdAt", "updatedAt");
    private static final Set<String> SUPPORTED_PREFERENCES = Set.of(
            "marketing_email",
            "marketing_sms",
            "preferred_locale",
            "preferred_currency",
            "default_catalog_view");

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final CustomerPreferenceRepository preferenceRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerAddressRepository addressRepository,
            CustomerPreferenceRepository preferenceRepository,
            LoyaltyAccountRepository loyaltyAccountRepository,
            CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.preferenceRepository = preferenceRepository;
        this.loyaltyAccountRepository = loyaltyAccountRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerSummaryResponse> search(
            String search,
            Boolean active,
            Pageable pageable) {
        validatePageable(pageable, CUSTOMER_SORT_FIELDS);
        Page<Customer> result = customerRepository.findAll(
                CustomerSpecifications.searchable(search, active),
                pageable);
        return PageResponse.from(result, customerMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse get(Long customerId) {
        Customer customer = findCustomer(customerId);
        return toDetail(customer);
    }

    @Transactional
    public CustomerDetailResponse create(CustomerCreateRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException(
                    "CUSTOMER_EMAIL_IN_USE",
                    "A customer already uses email " + email);
        }

        Customer customer = new Customer(
                request.name(),
                email,
                customerMapper.toHomeAddress(request.homeAddress()));
        customer = customerRepository.save(customer);
        loyaltyAccountRepository.save(new LoyaltyAccount(customer.getId()));
        return toDetail(customer);
    }

    @Transactional
    public CustomerDetailResponse update(Long customerId, CustomerUpdateRequest request) {
        Customer customer = findActiveCustomer(customerId);
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(email, customerId)) {
            throw new BusinessRuleException(
                    "CUSTOMER_EMAIL_IN_USE",
                    "A customer already uses email " + email);
        }

        customer.updateProfile(
                request.name(),
                email,
                customerMapper.toHomeAddress(request.homeAddress()));
        return toDetail(customer);
    }

    @Transactional
    public void deactivate(Long customerId) {
        Customer customer = findCustomer(customerId);
        if (customer.isActive()) {
            customer.deactivate();
        }
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> listAddresses(Long customerId) {
        findCustomer(customerId);
        return addressRepository
                .findAllByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(customerId)
                .stream()
                .map(customerMapper::toAddressResponse)
                .toList();
    }

    @Transactional
    public CustomerAddressResponse addAddress(
            Long customerId,
            CustomerAddressRequest request) {
        findActiveCustomer(customerId);
        long addressCount = addressRepository.countByCustomerId(customerId);
        if (addressCount >= MAX_ADDRESSES) {
            throw new BusinessRuleException(
                    "CUSTOMER_ADDRESS_LIMIT",
                    "A customer may have at most " + MAX_ADDRESSES + " addresses");
        }

        boolean makeDefault = addressCount == 0 || request.defaultAddress();
        if (makeDefault) {
            clearDefaultAddress(customerId, null);
        }

        CustomerAddress address = new CustomerAddress(
                customerId,
                request.type(),
                request.label(),
                request.street(),
                request.city(),
                request.state(),
                request.zip(),
                request.countryCode(),
                makeDefault);
        return customerMapper.toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public CustomerAddressResponse updateAddress(
            Long customerId,
            Long addressId,
            CustomerAddressRequest request) {
        findActiveCustomer(customerId);
        CustomerAddress address = findAddress(customerId, addressId);

        if (request.defaultAddress()) {
            clearDefaultAddress(customerId, addressId);
            address.makeDefault();
        } else if (address.isDefaultAddress()
                && addressRepository.countByCustomerId(customerId) > 1) {
            throw new BusinessRuleException(
                    "DEFAULT_ADDRESS_REQUIRED",
                    "Choose another default address before clearing the current default");
        }

        address.update(
                request.type(),
                request.label(),
                request.street(),
                request.city(),
                request.state(),
                request.zip(),
                request.countryCode());
        return customerMapper.toAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(Long customerId, Long addressId) {
        findActiveCustomer(customerId);
        CustomerAddress address = findAddress(customerId, addressId);
        boolean promoteReplacement = address.isDefaultAddress();
        addressRepository.delete(address);

        if (promoteReplacement) {
            addressRepository.findAllByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(customerId)
                    .stream()
                    .filter(candidate -> !candidate.getId().equals(addressId))
                    .findFirst()
                    .ifPresent(CustomerAddress::makeDefault);
        }
    }

    @Transactional(readOnly = true)
    public List<CustomerPreferenceResponse> listPreferences(Long customerId) {
        findCustomer(customerId);
        return preferenceRepository.findAllByCustomerIdOrderByKeyAsc(customerId)
                .stream()
                .map(customerMapper::toPreferenceResponse)
                .toList();
    }

    @Transactional
    public CustomerPreferenceResponse setPreference(
            Long customerId,
            String key,
            CustomerPreferenceRequest request) {
        findActiveCustomer(customerId);
        String normalizedKey = normalizePreferenceKey(key);
        String normalizedValue = validatePreferenceValue(normalizedKey, request.value());

        CustomerPreference preference = preferenceRepository
                .findByCustomerIdAndKey(customerId, normalizedKey)
                .orElseGet(() -> {
                    if (preferenceRepository.countByCustomerId(customerId) >= MAX_PREFERENCES) {
                        throw new BusinessRuleException(
                                "CUSTOMER_PREFERENCE_LIMIT",
                                "A customer may have at most "
                                        + MAX_PREFERENCES
                                        + " preferences");
                    }
                    return new CustomerPreference(customerId, normalizedKey, normalizedValue);
                });
        preference.changeValue(normalizedValue);
        return customerMapper.toPreferenceResponse(preferenceRepository.save(preference));
    }

    @Transactional
    public void deletePreference(Long customerId, String key) {
        findActiveCustomer(customerId);
        String normalizedKey = normalizePreferenceKey(key);
        if (preferenceRepository.findByCustomerIdAndKey(customerId, normalizedKey).isEmpty()) {
            throw new ResourceNotFoundException(
                    "Customer preference " + normalizedKey + " was not found");
        }
        preferenceRepository.deleteByCustomerIdAndKey(customerId, normalizedKey);
    }

    private CustomerDetailResponse toDetail(Customer customer) {
        return customerMapper.toDetail(
                customer,
                addressRepository.findAllByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(
                        customer.getId()),
                preferenceRepository.findAllByCustomerIdOrderByKeyAsc(customer.getId()));
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
    }

    private Customer findActiveCustomer(Long customerId) {
        Customer customer = findCustomer(customerId);
        if (!customer.isActive()) {
            throw new BusinessRuleException(
                    "CUSTOMER_INACTIVE",
                    "Customer " + customerId + " is inactive");
        }
        return customer;
    }

    private CustomerAddress findAddress(Long customerId, Long addressId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address " + addressId + " for customer " + customerId + " was not found"));
    }

    private void clearDefaultAddress(Long customerId, Long excludedAddressId) {
        addressRepository.findAllByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(customerId)
                .stream()
                .filter(CustomerAddress::isDefaultAddress)
                .filter(address -> excludedAddressId == null
                        || !address.getId().equals(excludedAddressId))
                .forEach(CustomerAddress::clearDefault);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePreferenceKey(String key) {
        if (key == null || key.isBlank() || key.length() > 80) {
            throw new BusinessRuleException(
                    "INVALID_PREFERENCE_KEY",
                    "Preference key must contain between 1 and 80 characters");
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_PREFERENCES.contains(normalized)) {
            throw new BusinessRuleException(
                    "UNSUPPORTED_PREFERENCE",
                    "Unsupported preference key " + normalized);
        }
        return normalized;
    }

    private static String validatePreferenceValue(String key, String value) {
        String normalized = value.trim();
        if (key.equals("marketing_email") || key.equals("marketing_sms")) {
            if (!normalized.equalsIgnoreCase("true")
                    && !normalized.equalsIgnoreCase("false")) {
                throw new BusinessRuleException(
                        "INVALID_PREFERENCE_VALUE",
                        key + " must be true or false");
            }
            return normalized.toLowerCase(Locale.ROOT);
        }
        if (key.equals("preferred_currency")) {
            String currency = normalized.toUpperCase(Locale.ROOT);
            if (!currency.matches("[A-Z]{3}")) {
                throw new BusinessRuleException(
                        "INVALID_PREFERENCE_VALUE",
                        "preferred_currency must be a three-letter ISO code");
            }
            return currency;
        }
        if (key.equals("preferred_locale")
                && !normalized.matches("[a-zA-Z]{2}([-_][a-zA-Z]{2})?")) {
            throw new BusinessRuleException(
                    "INVALID_PREFERENCE_VALUE",
                    "preferred_locale must be a language or language-region code");
        }
        if (key.equals("default_catalog_view")
                && !Set.of("grid", "list").contains(normalized.toLowerCase(Locale.ROOT))) {
            throw new BusinessRuleException(
                    "INVALID_PREFERENCE_VALUE",
                    "default_catalog_view must be grid or list");
        }
        return normalized;
    }

    private static void validatePageable(Pageable pageable, Set<String> allowedSortFields) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Page size may not exceed " + MAX_PAGE_SIZE);
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "INVALID_SORT",
                        "Sorting by " + order.getProperty() + " is not supported");
            }
        }
    }
}
