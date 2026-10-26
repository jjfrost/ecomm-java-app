package com.ecommerce.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeCreateRequest(
        @Size(max = 20) String firstName,
        @NotBlank @Size(max = 25) String lastName,
        @NotBlank @Email @Size(max = 25) String email,
        @Size(max = 20) String phoneNumber,
        @NotNull @PastOrPresent LocalDate hireDate,
        @NotBlank @Size(max = 10) String jobId,
        @PositiveOrZero BigDecimal salary,
        @DecimalMin("0.00") @DecimalMax("0.99") BigDecimal commissionPct,
        Long managerId,
        Long departmentId) {
}
