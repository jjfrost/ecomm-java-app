package com.ecommerce.workforce.repository;

import com.ecommerce.workforce.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {
    List<Country> findByRegionIdOrderByCountryName(Long regionId);
}
