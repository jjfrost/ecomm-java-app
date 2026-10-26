package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findAllByProductIdOrderByPrimaryImageDescSortOrderAsc(Long productId);

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);

    Optional<ProductImage> findFirstByProductIdAndPrimaryImageTrue(Long productId);

    List<ProductImage> findAllByProductIdIn(Collection<Long> productIds);

    long countByProductId(Long productId);

    void deleteAllByProductId(Long productId);
}
