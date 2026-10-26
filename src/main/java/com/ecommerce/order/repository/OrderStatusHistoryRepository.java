package com.ecommerce.order.repository;

import com.ecommerce.order.domain.OrderStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    Page<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(Long orderId, Pageable pageable);
}
