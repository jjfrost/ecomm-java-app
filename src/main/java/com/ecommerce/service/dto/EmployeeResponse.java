package com.ecommerce.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long employeeId,
        String firstName,
        String lastName,
        String displayName,
        String email,
        String phoneNumber,
        LocalDate hireDate,
        String jobId,
        BigDecimal salary,
        BigDecimal commissionPct,
        Long managerId,
        Long departmentId) {
}
