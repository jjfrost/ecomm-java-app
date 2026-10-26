package com.ecommerce.reporting.mapper;

import com.ecommerce.reporting.dto.ReportingDtos.CustomerLifetimeValueResponse;
import com.ecommerce.reporting.dto.ReportingDtos.ProductSalesResponse;
import com.ecommerce.reporting.model.CustomerLifetimeValueReport;
import com.ecommerce.reporting.model.ProductSalesReport;
import org.springframework.stereotype.Component;

@Component
public class ReportingMapper {
    public CustomerLifetimeValueResponse toResponse(CustomerLifetimeValueReport report) {
        return new CustomerLifetimeValueResponse(
                report.getCustomerId(),
                report.getCustomerName(),
                report.getEmail(),
                report.getOrderCount(),
                report.getTotalSpent(),
                report.getAverageOrderValue(),
                report.getFirstOrderAt(),
                report.getLastOrderAt(),
                report.getLifetimeValueTier());
    }

    public ProductSalesResponse toResponse(ProductSalesReport report) {
        return new ProductSalesResponse(
                report.getProductId(),
                report.getProductName(),
                report.getCategoryId(),
                report.getUnitsSold(),
                report.getOrderCount(),
                report.getGrossRevenue(),
                report.getNetRevenue(),
                report.getAverageUnitPrice(),
                report.getLastSoldAt());
    }
}
