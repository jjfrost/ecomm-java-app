package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.domain.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<ProductAttribute> findAllByOrderByNameAsc();

    List<ProductAttribute> findAllByRequiredTrueOrderByNameAsc();
}
