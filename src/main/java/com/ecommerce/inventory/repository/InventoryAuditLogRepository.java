package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.domain.InventoryAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryAuditLogRepository extends JpaRepository<InventoryAuditLog, Long> {
    Page<InventoryAuditLog> findByInventoryIdOrderByChangedAtDesc(Long inventoryId, Pageable pageable);
}
