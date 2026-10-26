package com.ecommerce.controller;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.service.EmployeeService;
import com.ecommerce.service.dto.DepartmentPayrollSummary;
import com.ecommerce.service.dto.EmployeeCreateRequest;
import com.ecommerce.service.dto.EmployeeResponse;
import com.ecommerce.service.dto.EmployeeUpdateRequest;
import com.ecommerce.service.dto.ManagerAssignmentRequest;
import com.ecommerce.service.dto.PromotionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployeeById(@PathVariable @Positive Long id) {
        return employeeService.getEmployeeById(id);
    }

    @GetMapping("/department/{departmentId}")
    public PageResponse<EmployeeResponse> getByDepartment(
            @PathVariable @Positive Long departmentId,
            Pageable pageable) {
        return employeeService.getEmployeesByDepartment(departmentId, pageable);
    }

    @GetMapping("/{id}/direct-reports")
    public List<EmployeeResponse> getDirectReports(@PathVariable @Positive Long id) {
        return employeeService.getDirectReports(id);
    }

    @GetMapping("/{id}/hierarchy")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public List<EmployeeResponse> getHierarchy(@PathVariable @Positive Long id) {
        return employeeService.getReportingHierarchy(id);
    }

    @GetMapping("/payroll-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DepartmentPayrollSummary> getPayrollSummary() {
        return employeeService.getPayrollSummary();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeResponse updateEmployee(
            @PathVariable @Positive Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable @Positive Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignManager(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ManagerAssignmentRequest request) {
        employeeService.assignManager(id, request.managerId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> promoteEmployee(
            @PathVariable @Positive Long id,
            @Valid @RequestBody PromotionRequest request) {
        employeeService.promoteEmployee(id, request.raisePercentage());
        return ResponseEntity.noContent().build();
    }
}
