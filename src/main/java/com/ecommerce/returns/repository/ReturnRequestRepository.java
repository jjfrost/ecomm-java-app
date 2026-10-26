package com.ecommerce.returns.repository;

import com.ecommerce.returns.model.ReturnRequest;
import com.ecommerce.returns.model.ReturnStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findByAuthorizationCode(String authorizationCode);

    @Query("""
            SELECT request
              FROM ReturnRequest request
             WHERE (:orderId IS NULL OR request.orderId = :orderId)
               AND (:customerId IS NULL OR request.customerId = :customerId)
               AND (:status IS NULL OR request.returnStatus = :status)
            """)
    Page<ReturnRequest> search(
            @Param("orderId") Long orderId,
            @Param("customerId") Long customerId,
            @Param("status") ReturnStatus status,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT request FROM ReturnRequest request WHERE request.returnRequestId = :requestId")
    Optional<ReturnRequest> findByIdForUpdate(@Param("requestId") Long requestId);
}
