package com.ecommerce.reporting.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.reporting.dto.ReportingDtos.CustomerLifetimeValueResponse;
import com.ecommerce.reporting.dto.ReportingDtos.ProductSalesResponse;
import com.ecommerce.reporting.mapper.ReportingMapper;
import com.ecommerce.reporting.repository.CustomerLifetimeValueRepository;
import com.ecommerce.reporting.repository.ProductSalesReportRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReportingService {
    private static final int MAX_PAGE_SIZE = 200;

    private final CustomerLifetimeValueRepository customerRepository;
    private final ProductSalesReportRepository productRepository;
    private final ReportingMapper reportingMapper;

    public ReportingService(
            CustomerLifetimeValueRepository customerRepository,
            ProductSalesReportRepository productRepository,
            ReportingMapper reportingMapper) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.reportingMapper = reportingMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerLifetimeValueResponse> findCustomerLifetimeValues(
            BigDecimal minimumValue,
            String tier,
            String query,
            Pageable pageable) {
        validateNonNegative(minimumValue, "minimumValue");
        validatePage(pageable);
        return PageResponse.from(
                customerRepository.search(
                        minimumValue,
                        normalize(tier),
                        normalize(query),
                        pageable),
                reportingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSalesResponse> findProductSales(
            Long categoryId,
            BigDecimal minimumRevenue,
            String query,
            Pageable pageable) {
        validateNonNegative(minimumRevenue, "minimumRevenue");
        validatePage(pageable);
        return PageResponse.from(
                productRepository.search(
                        categoryId,
                        minimumRevenue,
                        normalize(query),
                        pageable),
                reportingMapper::toResponse);
    }

    private void validateNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.signum() < 0) {
            throw new BusinessRuleException(
                    "NEGATIVE_REPORT_FILTER",
                    fieldName + " cannot be negative");
        }
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Report page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
