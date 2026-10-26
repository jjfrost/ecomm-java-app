package com.ecommerce.pricing.repository;

import com.ecommerce.pricing.domain.PriceList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndPriceListIdNot(String name, Long priceListId);
    Page<PriceList> findByActiveTrue(Pageable pageable);
}
