package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.domain.Warehouse;
import com.ecommerce.inventory.domain.WarehouseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByWarehouseCodeIgnoreCase(String warehouseCode);

    boolean existsByWarehouseCodeIgnoreCaseAndWarehouseIdNot(String warehouseCode, Long warehouseId);

    Page<Warehouse> findByStatus(WarehouseStatus status, Pageable pageable);
}
