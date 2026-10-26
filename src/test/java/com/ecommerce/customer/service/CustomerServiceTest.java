package com.ecommerce.customer.service;

import com.ecommerce.customer.api.dto.CustomerAddressRequest;
import com.ecommerce.customer.api.dto.CustomerAddressResponse;
import com.ecommerce.customer.domain.AddressType;
import com.ecommerce.customer.domain.Customer;
import com.ecommerce.customer.domain.CustomerAddress;
import com.ecommerce.customer.domain.HomeAddress;
import com.ecommerce.customer.mapper.CustomerMapper;
import com.ecommerce.customer.repository.CustomerAddressRepository;
import com.ecommerce.customer.repository.CustomerPreferenceRepository;
import com.ecommerce.customer.repository.CustomerRepository;
import com.ecommerce.customer.repository.LoyaltyAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAddressRepository addressRepository;

    @Mock
    private CustomerPreferenceRepository preferenceRepository;

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(
                customerRepository,
                addressRepository,
                preferenceRepository,
                loyaltyAccountRepository,
                new CustomerMapper());
    }

    @Test
    void firstAddressBecomesDefaultEvenWhenNotRequested() {
        Customer customer = new Customer(
                "Grace Hopper",
                "grace@example.com",
                new HomeAddress("1 Navy Way", "Arlington", "22201"));
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(addressRepository.countByCustomerId(7L)).thenReturn(0L);
        when(addressRepository.findAllByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(7L))
                .thenReturn(List.of());
        when(addressRepository.save(any(CustomerAddress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerAddressResponse response = customerService.addAddress(
                7L,
                new CustomerAddressRequest(
                        AddressType.SHIPPING,
                        " Office ",
                        " 10 Compiler Road ",
                        " New York ",
                        "NY",
                        "10001",
                        "us",
                        false));

        ArgumentCaptor<CustomerAddress> captor =
                ArgumentCaptor.forClass(CustomerAddress.class);
        verify(addressRepository).save(captor.capture());
        assertTrue(response.defaultAddress());
        assertTrue(captor.getValue().isDefaultAddress());
        assertEquals("US", response.countryCode());
        assertEquals("Office", response.label());
    }
}
