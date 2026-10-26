package com.ecommerce.workforce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "jobs", schema = "ecomm")
public class JobRole {
    @Id
    @Column(name = "job_id", length = 10)
    private String jobId;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "min_salary", precision = 10, scale = 2)
    private BigDecimal minimumSalary;

    @Column(name = "max_salary", precision = 10, scale = 2)
    private BigDecimal maximumSalary;

    public boolean acceptsSalary(BigDecimal salary) {
        if (salary == null) {
            return true;
        }
        boolean aboveMinimum = minimumSalary == null || salary.compareTo(minimumSalary) >= 0;
        boolean belowMaximum = maximumSalary == null || salary.compareTo(maximumSalary) <= 0;
        return aboveMinimum && belowMaximum;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public BigDecimal getMinimumSalary() {
        return minimumSalary;
    }

    public void setMinimumSalary(BigDecimal minimumSalary) {
        this.minimumSalary = minimumSalary;
    }

    public BigDecimal getMaximumSalary() {
        return maximumSalary;
    }

    public void setMaximumSalary(BigDecimal maximumSalary) {
        this.maximumSalary = maximumSalary;
    }
}
