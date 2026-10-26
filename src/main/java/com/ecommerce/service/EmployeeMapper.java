package com.ecommerce.service;

import com.ecommerce.model.Employee;
import com.ecommerce.service.dto.EmployeeCreateRequest;
import com.ecommerce.service.dto.EmployeeResponse;
import com.ecommerce.service.dto.EmployeeUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public Employee toEntity(EmployeeCreateRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setHireDate(request.hireDate());
        employee.setJobId(request.jobId());
        employee.setSalary(request.salary());
        employee.setCommissionPct(request.commissionPct());
        employee.setManagerId(request.managerId());
        employee.setDepartmentId(request.departmentId());
        return employee;
    }

    public void update(Employee employee, EmployeeUpdateRequest request) {
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setPhoneNumber(request.phoneNumber());
        employee.setHireDate(request.hireDate());
        employee.setJobId(request.jobId());
        employee.setSalary(request.salary());
        employee.setCommissionPct(request.commissionPct());
        employee.setManagerId(request.managerId());
        employee.setDepartmentId(request.departmentId());
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDisplayName(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getHireDate(),
                employee.getJobId(),
                employee.getSalary(),
                employee.getCommissionPct(),
                employee.getManagerId(),
                employee.getDepartmentId());
    }
}
