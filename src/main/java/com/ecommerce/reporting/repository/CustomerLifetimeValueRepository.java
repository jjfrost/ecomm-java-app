package com.ecommerce.reporting.repository;

import com.ecommerce.reporting.model.CustomerLifetimeValueReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface CustomerLifetimeValueRepository
        extends JpaRepository<CustomerLifetimeValueReport, Long> {
    @Query("""
            SELECT report
              FROM CustomerLifetimeValueReport report
             WHERE (:minimumValue IS NULL OR report.totalSpent >= :minimumValue)
               AND (:tier IS NULL OR UPPER(report.lifetimeValueTier) = UPPER(:tier))
               AND (:query IS NULL
                    OR UPPER(report.customerName) LIKE UPPER(CONCAT(CONCAT('%', :query), '%'))
                    OR UPPER(report.email) LIKE UPPER(CONCAT(CONCAT('%', :query), '%')))
            """)
    Page<CustomerLifetimeValueReport> search(
            @Param("minimumValue") BigDecimal minimumValue,
            @Param("tier") String tier,
            @Param("query") String query,
            Pageable pageable);
}
