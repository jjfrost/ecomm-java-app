package com.ecommerce.customer.repository;

import com.ecommerce.customer.domain.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findAllByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(Long customerId);

    Optional<CustomerAddress> findByIdAndCustomerId(Long id, Long customerId);

    Optional<CustomerAddress> findFirstByCustomerIdAndDefaultAddressTrue(Long customerId);

    long countByCustomerId(Long customerId);

    void deleteAllByCustomerId(Long customerId);
}
