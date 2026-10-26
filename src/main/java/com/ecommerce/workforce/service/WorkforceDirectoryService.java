package com.ecommerce.workforce.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.repository.EmployeeRepository;
import com.ecommerce.workforce.dto.WorkforceRequests.CountryRequest;
import com.ecommerce.workforce.dto.WorkforceRequests.DepartmentRequest;
import com.ecommerce.workforce.dto.WorkforceRequests.JobRoleRequest;
import com.ecommerce.workforce.dto.WorkforceRequests.LocationRequest;
import com.ecommerce.workforce.dto.WorkforceRequests.RegionRequest;
import com.ecommerce.workforce.model.Country;
import com.ecommerce.workforce.model.Department;
import com.ecommerce.workforce.model.JobRole;
import com.ecommerce.workforce.model.Location;
import com.ecommerce.workforce.model.Region;
import com.ecommerce.workforce.repository.CountryRepository;
import com.ecommerce.workforce.repository.DepartmentRepository;
import com.ecommerce.workforce.repository.JobRoleRepository;
import com.ecommerce.workforce.repository.LocationRepository;
import com.ecommerce.workforce.repository.RegionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class WorkforceDirectoryService {
    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;
    private final EmployeeRepository employeeRepository;

    public WorkforceDirectoryService(
            RegionRepository regionRepository,
            CountryRepository countryRepository,
            LocationRepository locationRepository,
            DepartmentRepository departmentRepository,
            JobRoleRepository jobRoleRepository,
            EmployeeRepository employeeRepository) {
        this.regionRepository = regionRepository;
        this.countryRepository = countryRepository;
        this.locationRepository = locationRepository;
        this.departmentRepository = departmentRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<Region> listRegions() {
        return regionRepository.findAll();
    }

    @Transactional
    public Region createRegion(RegionRequest request) {
        if (regionRepository.existsByRegionNameIgnoreCase(request.name())) {
            throw new BusinessRuleException("REGION_EXISTS", "Region name is already in use");
        }
        Region region = new Region();
        region.setRegionName(request.name().trim());
        return regionRepository.save(region);
    }

    @Transactional(readOnly = true)
    public List<Country> listCountries(Long regionId) {
        requireRegion(regionId);
        return countryRepository.findByRegionIdOrderByCountryName(regionId);
    }

    @Transactional
    public Country createCountry(CountryRequest request) {
        String countryId = request.countryId().toUpperCase(Locale.ROOT);
        if (countryRepository.existsById(countryId)) {
            throw new BusinessRuleException("COUNTRY_EXISTS", "Country code is already in use");
        }
        requireRegion(request.regionId());
        Country country = new Country();
        country.setCountryId(countryId);
        country.setCountryName(request.name().trim());
        country.setRegionId(request.regionId());
        return countryRepository.save(country);
    }

    @Transactional(readOnly = true)
    public List<Location> listLocations(String countryId) {
        String normalizedCountryId = countryId.toUpperCase(Locale.ROOT);
        requireCountry(normalizedCountryId);
        return locationRepository.findByCountryIdOrderByCity(normalizedCountryId);
    }

    @Transactional
    public Location createLocation(LocationRequest request) {
        String countryId = request.countryId().toUpperCase(Locale.ROOT);
        requireCountry(countryId);
        Location location = new Location();
        location.setStreetAddress(request.streetAddress());
        location.setPostalCode(request.postalCode());
        location.setCity(request.city().trim());
        location.setStateProvince(request.stateProvince());
        location.setCountryId(countryId);
        return locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    public PageResponse<Department> listDepartments(Long locationId, Pageable pageable) {
        if (locationId == null) {
            return PageResponse.from(departmentRepository.findAll(pageable));
        }
        requireLocation(locationId);
        return PageResponse.from(departmentRepository.findByLocationId(locationId, pageable));
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentNameIgnoreCase(request.name())) {
            throw new BusinessRuleException("DEPARTMENT_EXISTS", "Department name is already in use");
        }
        if (request.locationId() != null) {
            requireLocation(request.locationId());
        }
        if (request.managerId() != null && !employeeRepository.existsById(request.managerId())) {
            throw new ResourceNotFoundException("Employee", request.managerId());
        }
        Department department = new Department();
        department.setDepartmentName(request.name().trim());
        department.setManagerId(request.managerId());
        department.setLocationId(request.locationId());
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public List<JobRole> listJobRoles() {
        return jobRoleRepository.findAll();
    }

    @Transactional
    public JobRole createJobRole(JobRoleRequest request) {
        String jobId = request.jobId().toUpperCase(Locale.ROOT);
        if (jobRoleRepository.existsById(jobId)) {
            throw new BusinessRuleException("JOB_EXISTS", "Job identifier is already in use");
        }
        if (request.minimumSalary() != null
                && request.maximumSalary() != null
                && request.minimumSalary().compareTo(request.maximumSalary()) > 0) {
            throw new BusinessRuleException(
                    "INVALID_SALARY_RANGE",
                    "Minimum salary cannot exceed maximum salary");
        }
        JobRole role = new JobRole();
        role.setJobId(jobId);
        role.setJobTitle(request.title().trim());
        role.setMinimumSalary(request.minimumSalary());
        role.setMaximumSalary(request.maximumSalary());
        return jobRoleRepository.save(role);
    }

    private Region requireRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new ResourceNotFoundException("Region", regionId));
    }

    private Country requireCountry(String countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new ResourceNotFoundException("Country", countryId));
    }

    private Location requireLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
    }
}
