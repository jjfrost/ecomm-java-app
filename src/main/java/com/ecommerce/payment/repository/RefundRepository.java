package com.ecommerce.payment.repository;

import com.ecommerce.payment.model.Refund;
import com.ecommerce.payment.model.RefundStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Refund> findByPaymentId(Long paymentId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT refund FROM Refund refund WHERE refund.refundId = :refundId")
    Optional<Refund> findByIdForUpdate(@Param("refundId") Long refundId);

    @Query("""
            SELECT COALESCE(SUM(refund.amount), 0)
              FROM Refund refund
             WHERE refund.paymentId = :paymentId
               AND refund.refundStatus IN :statuses
            """)
    BigDecimal sumReservedAmount(
            @Param("paymentId") Long paymentId,
            @Param("statuses") Collection<RefundStatus> statuses);

    @Query(value = """
            SELECT refund_candidate.*
              FROM (
                    SELECT refund.*
                      FROM ecomm.refunds refund
                     WHERE refund.refund_status = 'PENDING'
                       AND refund.requested_at <= SYSTIMESTAMP
                     ORDER BY refund.requested_at,
                              refund.refund_id
                   ) refund_candidate
             WHERE ROWNUM <= :batchSize
               FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Refund> claimPendingRefunds(@Param("batchSize") int batchSize);
}
