package com.ecommerce.reporting.repository;

import com.ecommerce.reporting.model.ProductSalesReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductSalesReportRepository extends JpaRepository<ProductSalesReport, Long> {
    @Query("""
            SELECT report
              FROM ProductSalesReport report
             WHERE (:categoryId IS NULL OR report.categoryId = :categoryId)
               AND (:minimumRevenue IS NULL OR report.netRevenue >= :minimumRevenue)
               AND (:query IS NULL
                    OR UPPER(report.productName) LIKE UPPER(CONCAT(CONCAT('%', :query), '%')))
            """)
    Page<ProductSalesReport> search(
            @Param("categoryId") Long categoryId,
            @Param("minimumRevenue") BigDecimal minimumRevenue,
            @Param("query") String query,
            Pageable pageable);
}
