package com.ecommerce.payment.repository;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByProcessorTransactionId(String processorTransactionId);

    Optional<Payment> findByProcessorTransactionId(String processorTransactionId);

    Page<Payment> findByOrderId(Long orderId, Pageable pageable);

    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);

    Page<Payment> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    @Query("""
            SELECT payment
              FROM Payment payment
             WHERE (:orderId IS NULL OR payment.orderId = :orderId)
               AND (:customerId IS NULL OR payment.customerId = :customerId)
               AND (:status IS NULL OR payment.paymentStatus = :status)
            """)
    Page<Payment> search(
            @Param("orderId") Long orderId,
            @Param("customerId") Long customerId,
            @Param("status") PaymentStatus status,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT payment FROM Payment payment WHERE payment.paymentId = :paymentId")
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);

    @Query(value = """
            SELECT recent_payment.*
              FROM (
                    SELECT payment.*
                      FROM ecomm.payments payment
                     WHERE payment.payment_status = 'FAILED'
                       AND TRUNC(NVL(payment.paid_at, payment.created_at))
                               >= TRUNC(SYSDATE) - :lookbackDays
                       AND (
                            :reasonToken IS NULL
                            OR INSTR(
                                    UPPER(NVL(payment.failure_reason, '~')),
                                    UPPER(:reasonToken)) > 0
                       )
                     ORDER BY NVL(payment.paid_at, payment.created_at) DESC,
                              payment.payment_id DESC
                   ) recent_payment
             WHERE ROWNUM <= :maxRows
            """, nativeQuery = true)
    List<Payment> findRecentFailedPayments(
            @Param("lookbackDays") int lookbackDays,
            @Param("reasonToken") String reasonToken,
            @Param("maxRows") int maxRows);
}
