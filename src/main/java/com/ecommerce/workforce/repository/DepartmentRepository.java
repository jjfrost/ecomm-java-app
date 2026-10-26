package com.ecommerce.workforce.repository;

import com.ecommerce.workforce.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByDepartmentNameIgnoreCase(String departmentName);

    Page<Department> findByLocationId(Long locationId, Pageable pageable);
}
