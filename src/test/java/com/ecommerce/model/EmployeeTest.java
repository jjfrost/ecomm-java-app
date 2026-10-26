package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeTest {
    @Test
    void createsDisplayNameFromAvailableNames() {
        Employee employee = new Employee();
        employee.setFirstName("Ada");
        employee.setLastName("Lovelace");
        assertThat(employee.getDisplayName()).isEqualTo("Ada Lovelace");

        employee.setFirstName(null);
        assertThat(employee.getDisplayName()).isEqualTo("Lovelace");
    }

    @Test
    void preventsSelfManagement() {
        Employee employee = new Employee();
        employee.setEmployeeId(7L);

        assertThatThrownBy(() -> employee.assignManager(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("themselves");
    }

    @Test
    void appliesPromotionFields() {
        Employee employee = new Employee();

        employee.applyPromotion("SA_MAN", new BigDecimal("90000.00"));

        assertThat(employee.getJobId()).isEqualTo("SA_MAN");
        assertThat(employee.getSalary()).isEqualByComparingTo("90000.00");
    }
}
