package com.ecommerce.repository;

import com.ecommerce.model.Employee;
import com.ecommerce.service.dto.DepartmentPayrollSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndEmployeeIdNot(String email, Long employeeId);

    boolean existsByManagerId(Long managerId);

    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    List<Employee> findByManagerIdOrderByLastNameAscFirstNameAsc(Long managerId);

    @Query(value = """
            SELECT e.*
              FROM ecomm.employees e
             START WITH e.employee_id = :employeeId
           CONNECT BY PRIOR e.employee_id = e.manager_id
             ORDER SIBLINGS BY e.last_name, e.first_name
            """, nativeQuery = true)
    List<Employee> findReportingHierarchy(@Param("employeeId") Long employeeId);

    @Query(value = """
            SELECT department_id AS departmentId,
                   COUNT(*) AS employeeCount,
                   SUM(NVL(salary, 0)) AS totalSalary,
                   AVG(NVL(salary, 0)) AS averageSalary
              FROM ecomm.employees
             GROUP BY department_id
             ORDER BY department_id
            """, nativeQuery = true)
    List<DepartmentPayrollSummary> summarizePayrollByDepartment();
}
