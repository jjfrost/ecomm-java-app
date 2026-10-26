package com.ecommerce.customer.repository;

import com.ecommerce.customer.domain.LoyaltyAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByCustomerId(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from LoyaltyAccount account where account.customerId = :customerId")
    Optional<LoyaltyAccount> findByCustomerIdForUpdate(@Param("customerId") Long customerId);

    @Query(
            value = """
                    SELECT account.*
                    FROM ecomm.loyalty_accounts account
                    WHERE account.customer_id = :customerId
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true)
    Optional<LoyaltyAccount> findByCustomerIdForUpdateSkipLockedOracle(
            @Param("customerId") Long customerId);
}
