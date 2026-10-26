package com.ecommerce.customer.repository;

import com.ecommerce.customer.domain.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    Page<LoyaltyTransaction> findAllByCustomerId(Long customerId, Pageable pageable);

    boolean existsByCustomerIdAndExternalReference(Long customerId, String externalReference);

    @Query(
            value = """
                    SELECT recent_transaction.*
                    FROM (
                        SELECT loyalty_transaction.*
                        FROM ecomm.loyalty_transactions loyalty_transaction
                        WHERE loyalty_transaction.customer_id = :customerId
                          AND loyalty_transaction.created_at
                              >= TRUNC(SYSDATE) - NVL(:daysBack, 30)
                        ORDER BY
                            loyalty_transaction.created_at DESC,
                            loyalty_transaction.loyalty_transaction_id DESC
                    ) recent_transaction
                    WHERE ROWNUM <= :maxRows
                    """,
            nativeQuery = true)
    List<LoyaltyTransaction> findRecentForCustomerOracle(
            @Param("customerId") Long customerId,
            @Param("daysBack") Integer daysBack,
            @Param("maxRows") int maxRows);
}
