package com.ecommerce.workforce.repository;

import com.ecommerce.workforce.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
    boolean existsByRegionNameIgnoreCase(String regionName);
}
