package com.ecommerce.supplier.repository;

import com.ecommerce.supplier.model.Supplier;
import com.ecommerce.supplier.model.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsBySupplierCodeIgnoreCase(String supplierCode);

    boolean existsByContactEmailIgnoreCase(String contactEmail);

    boolean existsByContactEmailIgnoreCaseAndSupplierIdNot(String contactEmail, Long supplierId);

    @Query("""
            SELECT supplier
              FROM Supplier supplier
             WHERE (:status IS NULL OR supplier.supplierStatus = :status)
               AND (:query IS NULL
                    OR UPPER(supplier.supplierCode) LIKE UPPER(CONCAT(CONCAT('%', :query), '%'))
                    OR UPPER(supplier.supplierName) LIKE UPPER(CONCAT(CONCAT('%', :query), '%')))
            """)
    Page<Supplier> search(
            @Param("status") SupplierStatus status,
            @Param("query") String query,
            Pageable pageable);
}
