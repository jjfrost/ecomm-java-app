package com.ecommerce.customer.repository;

import com.ecommerce.customer.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query(
            value = """
                    SELECT customer.*
                    FROM ecomm.customers customer
                    WHERE customer.is_active = NVL(:activeFlag, customer.is_active)
                      AND (
                          NVL(:searchTerm, ' ') = ' '
                          OR INSTR(LOWER(customer.name), LOWER(:searchTerm)) > 0
                          OR INSTR(LOWER(customer.email), LOWER(:searchTerm)) > 0
                      )
                    ORDER BY
                        DECODE(customer.is_active, 'Y', 0, 1),
                        customer.customer_id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM ecomm.customers customer
                    WHERE customer.is_active = NVL(:activeFlag, customer.is_active)
                      AND (
                          NVL(:searchTerm, ' ') = ' '
                          OR INSTR(LOWER(customer.name), LOWER(:searchTerm)) > 0
                          OR INSTR(LOWER(customer.email), LOWER(:searchTerm)) > 0
                      )
                    """,
            nativeQuery = true)
    Page<Customer> searchOracle(
            @Param("searchTerm") String searchTerm,
            @Param("activeFlag") String activeFlag,
            Pageable pageable);
}
