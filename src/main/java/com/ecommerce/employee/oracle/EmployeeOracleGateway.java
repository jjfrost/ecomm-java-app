package com.ecommerce.employee.oracle;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.service.EmployeeCommandGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmployeeOracleGateway implements EmployeeCommandGateway {
    private static final String ASSIGN_MANAGER_ROUTINE = "ecomm.HR_PKG.Assign_Manager";
    private static final String PROMOTE_EMPLOYEE_ROUTINE = "ecomm.HR_PKG.Promote_Employee";

    private final OraclePackageExecutor packageExecutor;

    public EmployeeOracleGateway(OraclePackageExecutor packageExecutor) {
        this.packageExecutor = packageExecutor;
    }

    @Override
    public void assignManager(Long employeeId, Long managerId) {
        packageExecutor.execute(
                ASSIGN_MANAGER_ROUTINE,
                "{call ecomm.HR_PKG.Assign_Manager(?, ?)}",
                statement -> {
                    statement.setLong(1, employeeId);
                    statement.setLong(2, managerId);
                });
    }

    @Override
    public void promoteEmployee(Long employeeId, double raisePercentage) {
        packageExecutor.execute(
                PROMOTE_EMPLOYEE_ROUTINE,
                "{call ecomm.HR_PKG.Promote_Employee(?, ?)}",
                statement -> {
                    statement.setLong(1, employeeId);
                    statement.setBigDecimal(2, BigDecimal.valueOf(raisePercentage));
                });
    }
}
