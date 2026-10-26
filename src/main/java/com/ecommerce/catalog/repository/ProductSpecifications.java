package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.domain.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecifications {
    private ProductSpecifications() {
    }

    public static Specification<Product> searchable(
            String search,
            Long categoryId,
            Boolean active,
            BigDecimal minimumPrice,
            BigDecimal maximumPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")),
                                pattern)));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            if (minimumPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        minimumPrice));
            }
            if (maximumPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        maximumPrice));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
