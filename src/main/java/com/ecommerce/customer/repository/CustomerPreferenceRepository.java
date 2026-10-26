package com.ecommerce.customer.repository;

import com.ecommerce.customer.domain.CustomerPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerPreferenceRepository extends JpaRepository<CustomerPreference, Long> {
    List<CustomerPreference> findAllByCustomerIdOrderByKeyAsc(Long customerId);

    Optional<CustomerPreference> findByCustomerIdAndKey(Long customerId, String key);

    long countByCustomerId(Long customerId);

    void deleteByCustomerIdAndKey(Long customerId, String key);
}
