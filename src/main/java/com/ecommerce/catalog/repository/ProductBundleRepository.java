package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.domain.ProductBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductBundleRepository extends JpaRepository<ProductBundle, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query(
            value = """
                    SELECT bundle.*
                    FROM ecomm.product_bundles bundle
                    WHERE bundle.bundle_id IN (
                        SELECT bundle_item.bundle_id
                        FROM ecomm.product_bundle_items bundle_item
                        JOIN ecomm.products product
                          ON product.product_id = bundle_item.product_id
                        GROUP BY bundle_item.bundle_id
                        HAVING INSTR(
                            LISTAGG(UPPER(product.sku), ',')
                                WITHIN GROUP (ORDER BY UPPER(product.sku)),
                            UPPER(:skuFragment)
                        ) > 0
                    )
                    ORDER BY bundle.bundle_id
                    """,
            nativeQuery = true)
    List<ProductBundle> findByComponentSkuFragmentOracle(
            @Param("skuFragment") String skuFragment);
}
