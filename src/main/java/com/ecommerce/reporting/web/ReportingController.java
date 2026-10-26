package com.ecommerce.reporting.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.reporting.dto.ReportingDtos.CustomerLifetimeValueResponse;
import com.ecommerce.reporting.dto.ReportingDtos.ProductSalesResponse;
import com.ecommerce.reporting.service.ReportingService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
public class ReportingController {
    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/customer-lifetime-value")
    public PageResponse<CustomerLifetimeValueResponse> findCustomerLifetimeValues(
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal minimumValue,
            @RequestParam(required = false) @Size(max = 40) String tier,
            @RequestParam(required = false) @Size(max = 200) String query,
            @PageableDefault(size = 50, sort = "totalSpent") Pageable pageable) {
        return reportingService.findCustomerLifetimeValues(minimumValue, tier, query, pageable);
    }

    @GetMapping("/product-sales")
    public PageResponse<ProductSalesResponse> findProductSales(
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal minimumRevenue,
            @RequestParam(required = false) @Size(max = 200) String query,
            @PageableDefault(size = 50, sort = "netRevenue") Pageable pageable) {
        return reportingService.findProductSales(categoryId, minimumRevenue, query, pageable);
    }
}
