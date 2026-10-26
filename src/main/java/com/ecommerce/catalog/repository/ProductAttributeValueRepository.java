package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.domain.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeValueRepository
        extends JpaRepository<ProductAttributeValue, Long> {

    List<ProductAttributeValue> findAllByProductId(Long productId);

    Optional<ProductAttributeValue> findByProductIdAndAttributeId(
            Long productId,
            Long attributeId);

    boolean existsByAttributeId(Long attributeId);

    void deleteAllByProductId(Long productId);
}
