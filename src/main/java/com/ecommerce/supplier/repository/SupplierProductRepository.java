package com.ecommerce.supplier.repository;

import com.ecommerce.supplier.model.SupplierProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {
    boolean existsBySupplierIdAndProductId(Long supplierId, Long productId);

    boolean existsBySupplierIdAndSupplierSkuIgnoreCase(Long supplierId, String supplierSku);

    boolean existsBySupplierIdAndSupplierSkuIgnoreCaseAndSupplierProductIdNot(
            Long supplierId,
            String supplierSku,
            Long supplierProductId);

    Optional<SupplierProduct> findBySupplierProductIdAndSupplierId(
            Long supplierProductId,
            Long supplierId);

    @Query("""
            SELECT product
              FROM SupplierProduct product
             WHERE (:supplierId IS NULL OR product.supplierId = :supplierId)
               AND (:productId IS NULL OR product.productId = :productId)
               AND (:active IS NULL OR product.active = :active)
            """)
    Page<SupplierProduct> search(
            @Param("supplierId") Long supplierId,
            @Param("productId") Long productId,
            @Param("active") Boolean active,
            Pageable pageable);
}
