package com.ecommerce.service.dto;

import java.math.BigDecimal;

public interface DepartmentPayrollSummary {
    Long getDepartmentId();

    Long getEmployeeCount();

    BigDecimal getTotalSalary();

    BigDecimal getAverageSalary();
}
