package com.ecommerce.service;

import com.ecommerce.model.Employee;
import com.ecommerce.service.dto.EmployeeCreateRequest;
import com.ecommerce.service.dto.EmployeeResponse;
import com.ecommerce.service.dto.EmployeeUpdateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {
    private final EmployeeMapper mapper = new EmployeeMapper();

    @Test
    void mapsCreateRequestToEntity() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Grace",
                "Hopper",
                "grace@example.com",
                "555-0100",
                LocalDate.of(2020, 1, 2),
                "IT_ARCH",
                new BigDecimal("125000.00"),
                null,
                3L,
                4L);

        Employee employee = mapper.toEntity(request);

        assertThat(employee.getFirstName()).isEqualTo("Grace");
        assertThat(employee.getLastName()).isEqualTo("Hopper");
        assertThat(employee.getEmail()).isEqualTo("grace@example.com");
        assertThat(employee.getManagerId()).isEqualTo(3L);
        assertThat(employee.getDepartmentId()).isEqualTo(4L);
    }

    @Test
    void updatesExistingEntityAndMapsResponse() {
        Employee employee = new Employee();
        employee.setEmployeeId(11L);
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "Katherine",
                "Johnson",
                "kj@example.com",
                "555-0101",
                LocalDate.of(2021, 4, 5),
                "FIN_MGR",
                new BigDecimal("110000.00"),
                new BigDecimal("0.10"),
                2L,
                8L);

        mapper.update(employee, request);
        EmployeeResponse response = mapper.toResponse(employee);

        assertThat(response.employeeId()).isEqualTo(11L);
        assertThat(response.displayName()).isEqualTo("Katherine Johnson");
        assertThat(response.salary()).isEqualByComparingTo("110000.00");
        assertThat(response.commissionPct()).isEqualByComparingTo("0.10");
    }
}
