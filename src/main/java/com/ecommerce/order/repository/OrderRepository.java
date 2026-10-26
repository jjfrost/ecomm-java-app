package com.ecommerce.order.repository;

import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CustomerOrder o where o.orderId = :orderId")
    Optional<Order> findLockedById(@Param("orderId") Long orderId);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    @Query(value = """
            SELECT o.*
              FROM ecomm.orders o
             WHERE o.order_id = (
                       SELECT queued.order_id
                         FROM (
                               SELECT candidate.order_id
                                 FROM ecomm.orders candidate
                                WHERE DECODE(candidate.status, :status, 1, 0) = 1
                                  AND TRUNC(candidate.order_date) = TRUNC(SYSDATE)
                                ORDER BY NVL(candidate.updated_at, candidate.created_at),
                                         candidate.order_id
                              ) queued
                        WHERE ROWNUM = 1
                   )
             FOR UPDATE NOWAIT
            """, nativeQuery = true)
    Optional<Order> lockNextOracleOrderForTodayNowait(
            @Param("status") String status);
}
