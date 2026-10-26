package com.ecommerce.service;

public interface EmployeeCommandGateway {
    void assignManager(Long employeeId, Long managerId);

    void promoteEmployee(Long employeeId, double raisePercentage);
}
