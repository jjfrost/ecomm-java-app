package com.ecommerce.workforce.controller;

import com.ecommerce.common.api.PageResponse;
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
import com.ecommerce.workforce.service.WorkforceDirectoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/workforce")
@PreAuthorize("hasRole('ADMIN')")
public class WorkforceDirectoryController {
    private final WorkforceDirectoryService workforceService;

    public WorkforceDirectoryController(WorkforceDirectoryService workforceService) {
        this.workforceService = workforceService;
    }

    @GetMapping("/regions")
    public List<Region> listRegions() {
        return workforceService.listRegions();
    }

    @PostMapping("/regions")
    public ResponseEntity<Region> createRegion(@Valid @RequestBody RegionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.createRegion(request));
    }

    @GetMapping("/countries")
    public List<Country> listCountries(@RequestParam Long regionId) {
        return workforceService.listCountries(regionId);
    }

    @PostMapping("/countries")
    public ResponseEntity<Country> createCountry(@Valid @RequestBody CountryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.createCountry(request));
    }

    @GetMapping("/locations")
    public List<Location> listLocations(@RequestParam String countryId) {
        return workforceService.listLocations(countryId);
    }

    @PostMapping("/locations")
    public ResponseEntity<Location> createLocation(@Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.createLocation(request));
    }

    @GetMapping("/departments")
    public PageResponse<Department> listDepartments(
            @RequestParam(required = false) Long locationId,
            Pageable pageable) {
        return workforceService.listDepartments(locationId, pageable);
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.createDepartment(request));
    }

    @GetMapping("/jobs")
    public List<JobRole> listJobs() {
        return workforceService.listJobRoles();
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobRole> createJob(@Valid @RequestBody JobRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workforceService.createJobRole(request));
    }
}
