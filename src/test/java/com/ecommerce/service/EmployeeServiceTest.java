package com.ecommerce.service;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.model.Employee;
import com.ecommerce.repository.EmployeeRepository;
import com.ecommerce.service.dto.EmployeeCreateRequest;
import com.ecommerce.service.dto.EmployeeResponse;
import com.ecommerce.service.dto.EmployeeUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository repository;

    @Mock
    private EmployeeCommandGateway employeeCommandGateway;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(repository, new EmployeeMapper(), employeeCommandGateway);
    }

    @Test
    void returnsAllEmployeesAsResponses() {
        Employee employee = employee(1L, "ada@example.com");
        when(repository.findAll()).thenReturn(List.of(employee));

        List<EmployeeResponse> result = service.getAllEmployees();

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.employeeId()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("ada@example.com");
        });
    }

    @Test
    void createsEmployeeAfterValidatingEmailAndManager() {
        Employee manager = employee(2L, "manager@example.com");
        when(repository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(repository.findById(2L)).thenReturn(Optional.of(manager));
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setEmployeeId(10L);
            return saved;
        });

        EmployeeResponse response = service.createEmployee(createRequest(2L));

        assertThat(response.employeeId()).isEqualTo(10L);
        assertThat(response.managerId()).isEqualTo(2L);
        verify(repository).save(any(Employee.class));
    }

    @Test
    void rejectsDuplicateEmployeeEmail() {
        when(repository.existsByEmailIgnoreCase("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createEmployee(createRequest(null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already registered");

        verify(repository, never()).save(any());
    }

    @Test
    void updatesEmployeeWhenEmailRemainsUnique() {
        Employee employee = employee(4L, "old@example.com");
        when(repository.findById(4L)).thenReturn(Optional.of(employee));
        when(repository.existsByEmailIgnoreCaseAndEmployeeIdNot("updated@example.com", 4L))
                .thenReturn(false);
        when(repository.save(employee)).thenReturn(employee);
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "Updated",
                "Person",
                "updated@example.com",
                "555-0199",
                LocalDate.of(2022, 2, 2),
                "SA_REP",
                new BigDecimal("80000"),
                null,
                null,
                9L);

        EmployeeResponse response = service.updateEmployee(4L, request);

        assertThat(response.email()).isEqualTo("updated@example.com");
        assertThat(response.departmentId()).isEqualTo(9L);
    }

    @Test
    void refusesToDeleteManagerWithDirectReports() {
        Employee manager = employee(5L, "manager@example.com");
        when(repository.findById(5L)).thenReturn(Optional.of(manager));
        when(repository.existsByManagerId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteEmployee(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("direct reports");

        verify(repository, never()).delete(any());
    }

    @Test
    void assignsManagerThroughOraclePackage() {
        Employee employee = employee(6L, "employee@example.com");
        Employee manager = employee(7L, "manager@example.com");
        when(repository.findById(6L)).thenReturn(Optional.of(employee));
        when(repository.findById(7L)).thenReturn(Optional.of(manager));

        service.assignManager(6L, 7L);

        verify(employeeCommandGateway).assignManager(6L, 7L);
    }

    @Test
    void rejectsInvalidPromotionPercentage() {
        when(repository.findById(8L)).thenReturn(Optional.of(employee(8L, "person@example.com")));

        assertThatThrownBy(() -> service.promoteEmployee(8L, 101))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no more than 100");

        verify(employeeCommandGateway, never()).promoteEmployee(any(), anyDouble());
    }

    @Test
    void raisesNotFoundForUnknownEmployee() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEmployeeById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    private EmployeeCreateRequest createRequest(Long managerId) {
        return new EmployeeCreateRequest(
                "New",
                "Person",
                "new@example.com",
                null,
                LocalDate.of(2023, 1, 1),
                "SA_REP",
                new BigDecimal("70000"),
                null,
                managerId,
                3L);
    }

    private Employee employee(Long id, String email) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setEmail(email);
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setJobId("SA_REP");
        return employee;
    }
}
