package com.ecommerce.workforce.repository;

import com.ecommerce.workforce.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByCountryIdOrderByCity(String countryId);
}
