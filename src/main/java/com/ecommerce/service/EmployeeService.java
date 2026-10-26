package com.ecommerce.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.model.Employee;
import com.ecommerce.repository.EmployeeRepository;
import com.ecommerce.service.dto.DepartmentPayrollSummary;
import com.ecommerce.service.dto.EmployeeCreateRequest;
import com.ecommerce.service.dto.EmployeeResponse;
import com.ecommerce.service.dto.EmployeeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeCommandGateway employeeCommandGateway;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeeMapper employeeMapper,
            EmployeeCommandGateway employeeCommandGateway) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.employeeCommandGateway = employeeCommandGateway;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return employeeMapper.toResponse(requireEmployee(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getEmployeesByDepartment(Long departmentId, Pageable pageable) {
        Page<Employee> page = employeeRepository.findByDepartmentId(departmentId, pageable);
        return PageResponse.from(page, employeeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getDirectReports(Long managerId) {
        requireEmployee(managerId);
        return employeeRepository.findByManagerIdOrderByLastNameAscFirstNameAsc(managerId)
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getReportingHierarchy(Long employeeId) {
        requireEmployee(employeeId);
        return employeeRepository.findReportingHierarchy(employeeId)
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentPayrollSummary> getPayrollSummary() {
        return employeeRepository.summarizePayrollByDepartment();
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("EMPLOYEE_EMAIL_EXISTS", "Employee email is already registered");
        }
        validateManager(null, request.managerId());
        Employee employee = employeeMapper.toEntity(request);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = requireEmployee(id);
        if (employeeRepository.existsByEmailIgnoreCaseAndEmployeeIdNot(request.email(), id)) {
            throw new BusinessRuleException("EMPLOYEE_EMAIL_EXISTS", "Employee email is already registered");
        }
        validateManager(id, request.managerId());
        employeeMapper.update(employee, request);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = requireEmployee(id);
        if (employeeRepository.existsByManagerId(id)) {
            throw new BusinessRuleException(
                    "EMPLOYEE_HAS_REPORTS",
                    "An employee with direct reports cannot be deleted");
        }
        employeeRepository.delete(employee);
    }

    @Transactional
    public void assignManager(Long employeeId, Long managerId) {
        validateManager(employeeId, managerId);
        requireEmployee(employeeId);
        employeeCommandGateway.assignManager(employeeId, managerId);
    }

    @Transactional
    public void promoteEmployee(Long employeeId, double raisePercentage) {
        requireEmployee(employeeId);
        if (raisePercentage <= 0 || raisePercentage > 100) {
            throw new BusinessRuleException(
                    "INVALID_RAISE_PERCENTAGE",
                    "Raise percentage must be greater than zero and no more than 100");
        }
        employeeCommandGateway.promoteEmployee(employeeId, raisePercentage);
    }

    private Employee requireEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
    }

    private void validateManager(Long employeeId, Long managerId) {
        if (managerId == null) {
            return;
        }
        if (managerId.equals(employeeId)) {
            throw new BusinessRuleException(
                    "EMPLOYEE_SELF_MANAGEMENT",
                    "An employee cannot report to themselves");
        }
        requireEmployee(managerId);
    }
}
