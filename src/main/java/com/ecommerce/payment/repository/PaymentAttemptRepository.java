package com.ecommerce.payment.repository;

import com.ecommerce.payment.model.PaymentAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    long countByPaymentId(Long paymentId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<PaymentAttempt> findByPaymentId(Long paymentId, Pageable pageable);
}
