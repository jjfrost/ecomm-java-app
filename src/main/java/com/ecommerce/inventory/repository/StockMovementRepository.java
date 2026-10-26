package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.domain.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId, Pageable pageable);
}
