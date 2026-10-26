package com.ecommerce.pricing.repository;

import com.ecommerce.pricing.domain.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndCouponIdNot(String code, Long couponId);
    boolean existsByPromotionId(Long promotionId);
    Page<Coupon> findByPromotionId(Long promotionId, Pageable pageable);

    @Query(value = """
            SELECT c.*
              FROM ecomm.coupons c
             WHERE INSTR(UPPER(c.code), UPPER(:codeToken)) > 0
               AND DECODE(c.is_active, 'Y', 1, 0) = 1
               AND SYSTIMESTAMP >= c.valid_from
               AND SYSTIMESTAMP < c.valid_to
               AND NVL(c.usage_count, 0)
                   < NVL(c.usage_limit, NVL(c.usage_count, 0) + 1)
             ORDER BY c.valid_to, c.coupon_id
             FETCH FIRST 1 ROW ONLY
            """, nativeQuery = true)
    Optional<Coupon> findFirstOracleRedeemableCouponContaining(
            @Param("codeToken") String codeToken);
}
