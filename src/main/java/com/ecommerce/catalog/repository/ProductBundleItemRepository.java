package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.domain.ProductBundleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBundleItemRepository extends JpaRepository<ProductBundleItem, Long> {
    List<ProductBundleItem> findAllByBundleIdOrderByIdAsc(Long bundleId);

    List<ProductBundleItem> findAllByProductId(Long productId);

    boolean existsByProductId(Long productId);

    void deleteAllByBundleId(Long bundleId);
}
