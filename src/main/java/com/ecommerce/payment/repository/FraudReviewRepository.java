package com.ecommerce.payment.repository;

import com.ecommerce.payment.model.FraudReview;
import com.ecommerce.payment.model.FraudReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FraudReviewRepository extends JpaRepository<FraudReview, Long> {
    boolean existsByPaymentIdAndReviewStatusIn(
            Long paymentId,
            Collection<FraudReviewStatus> reviewStatuses);

    Page<FraudReview> findByReviewStatus(FraudReviewStatus reviewStatus, Pageable pageable);

    Page<FraudReview> findByCustomerId(Long customerId, Pageable pageable);

    @Query("""
            SELECT review
              FROM FraudReview review
             WHERE (:status IS NULL OR review.reviewStatus = :status)
               AND (:customerId IS NULL OR review.customerId = :customerId)
            """)
    Page<FraudReview> search(
            @Param("status") FraudReviewStatus status,
            @Param("customerId") Long customerId,
            Pageable pageable);

    @Query(value = """
            SELECT fraud_review.*
              FROM ecomm.fraud_review_queue fraud_review
             WHERE fraud_review.review_status IN ('PENDING', 'IN_REVIEW')
               AND JSON_VALUE(
                       fraud_review.risk_reason,
                       '$.ruleCode' RETURNING VARCHAR2(100) NULL ON ERROR) = :ruleCode
             ORDER BY fraud_review.risk_score DESC,
                      fraud_review.queued_at
             FETCH FIRST :maxRows ROWS ONLY
            """, nativeQuery = true)
    List<FraudReview> findOpenReviewsByJsonRule(
            @Param("ruleCode") String ruleCode,
            @Param("maxRows") int maxRows);
}
