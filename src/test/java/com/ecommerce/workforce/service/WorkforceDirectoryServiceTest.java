package com.ecommerce.workforce.service;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.repository.EmployeeRepository;
import com.ecommerce.workforce.dto.WorkforceRequests.CountryRequest;
import com.ecommerce.workforce.dto.WorkforceRequests.DepartmentRequest;
import com.ecommerce.workforce.dto.WorkforceRequests.JobRoleRequest;
import com.ecommerce.workforce.dto.WorkforceRequests.RegionRequest;
import com.ecommerce.workforce.model.Country;
import com.ecommerce.workforce.model.JobRole;
import com.ecommerce.workforce.model.Region;
import com.ecommerce.workforce.repository.CountryRepository;
import com.ecommerce.workforce.repository.DepartmentRepository;
import com.ecommerce.workforce.repository.JobRoleRepository;
import com.ecommerce.workforce.repository.LocationRepository;
import com.ecommerce.workforce.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkforceDirectoryServiceTest {
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private JobRoleRepository jobRoleRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private WorkforceDirectoryService service;

    @BeforeEach
    void setUp() {
        service = new WorkforceDirectoryService(
                regionRepository,
                countryRepository,
                locationRepository,
                departmentRepository,
                jobRoleRepository,
                employeeRepository);
    }

    @Test
    void createsUniqueRegion() {
        when(regionRepository.existsByRegionNameIgnoreCase("Europe")).thenReturn(false);
        when(regionRepository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Region region = service.createRegion(new RegionRequest("Europe"));

        assertThat(region.getRegionName()).isEqualTo("Europe");
    }

    @Test
    void rejectsDuplicateRegion() {
        when(regionRepository.existsByRegionNameIgnoreCase("Europe")).thenReturn(true);

        assertThatThrownBy(() -> service.createRegion(new RegionRequest("Europe")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void normalizesCountryIdentifier() {
        Region region = new Region();
        region.setRegionId(3L);
        when(regionRepository.findById(3L)).thenReturn(Optional.of(region));
        when(countryRepository.existsById("US")).thenReturn(false);
        when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Country country = service.createCountry(new CountryRequest("us", "United States", 3L));

        assertThat(country.getCountryId()).isEqualTo("US");
        assertThat(country.getRegionId()).isEqualTo(3L);
    }

    @Test
    void rejectsDepartmentWithUnknownManager() {
        when(departmentRepository.existsByDepartmentNameIgnoreCase("Operations")).thenReturn(false);
        when(employeeRepository.existsById(77L)).thenReturn(false);

        assertThatThrownBy(() -> service.createDepartment(new DepartmentRequest("Operations", 77L, null)))
                .hasMessageContaining("77");

        verify(departmentRepository, never()).save(any());
    }

    @Test
    void rejectsInvertedSalaryRange() {
        JobRoleRequest request = new JobRoleRequest(
                "DEV",
                "Developer",
                new BigDecimal("120000"),
                new BigDecimal("90000"));
        when(jobRoleRepository.existsById("DEV")).thenReturn(false);

        assertThatThrownBy(() -> service.createJobRole(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Minimum salary");
    }

    @Test
    void createsValidJobRole() {
        JobRoleRequest request = new JobRoleRequest(
                "dev",
                "Developer",
                new BigDecimal("70000"),
                new BigDecimal("130000"));
        when(jobRoleRepository.existsById("DEV")).thenReturn(false);
        when(jobRoleRepository.save(any(JobRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobRole role = service.createJobRole(request);

        assertThat(role.getJobId()).isEqualTo("DEV");
        assertThat(role.acceptsSalary(new BigDecimal("100000"))).isTrue();
        assertThat(role.acceptsSalary(new BigDecimal("150000"))).isFalse();
    }
}
