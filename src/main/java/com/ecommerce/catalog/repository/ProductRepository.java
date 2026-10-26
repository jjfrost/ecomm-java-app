package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<Product> findBySkuIgnoreCase(String sku);

    long countByCategoryId(Long categoryId);

    long countByActiveTrue();

    @Query(
            value = """
                    SELECT product.*
                    FROM ecomm.products product
                    WHERE product.category_id = NVL(:categoryId, product.category_id)
                    ORDER BY
                        DECODE(product.is_active, 'N', 0, 1),
                        product.updated_at DESC,
                        product.product_id
                    FETCH FIRST 50 ROWS ONLY
                    """,
            nativeQuery = true)
    List<Product> findCatalogReviewQueueOracle(@Param("categoryId") Long categoryId);
}
