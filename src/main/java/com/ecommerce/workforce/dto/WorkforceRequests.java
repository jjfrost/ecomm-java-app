package com.ecommerce.workforce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class WorkforceRequests {
    private WorkforceRequests() {
    }

    public record RegionRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record CountryRequest(
            @NotBlank @Size(min = 2, max = 2) String countryId,
            @NotBlank @Size(max = 100) String name,
            @NotNull Long regionId) {
    }

    public record LocationRequest(
            @Size(max = 200) String streetAddress,
            @Size(max = 20) String postalCode,
            @NotBlank @Size(max = 100) String city,
            @Size(max = 100) String stateProvince,
            @NotBlank @Size(min = 2, max = 2) String countryId) {
    }

    public record DepartmentRequest(
            @NotBlank @Size(max = 100) String name,
            Long managerId,
            Long locationId) {
    }

    public record JobRoleRequest(
            @NotBlank @Size(max = 10) String jobId,
            @NotBlank @Size(max = 100) String title,
            @PositiveOrZero BigDecimal minimumSalary,
            @PositiveOrZero BigDecimal maximumSalary) {
    }
}
