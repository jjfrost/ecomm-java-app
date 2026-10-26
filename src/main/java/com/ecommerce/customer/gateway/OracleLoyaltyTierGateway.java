package com.ecommerce.customer.gateway;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.customer.domain.LoyaltyTier;
import org.springframework.stereotype.Component;

import java.sql.Types;

@Component
public class OracleLoyaltyTierGateway implements LoyaltyTierGateway {
    private static final String ROUTINE_NAME = "ecomm.LOYALTY_PKG.CALCULATE_TIER";
    private static final String CALL_SYNTAX =
            "{ ? = call ecomm.LOYALTY_PKG.CALCULATE_TIER(?) }";

    private final OraclePackageExecutor oraclePackageExecutor;

    public OracleLoyaltyTierGateway(OraclePackageExecutor oraclePackageExecutor) {
        this.oraclePackageExecutor = oraclePackageExecutor;
    }

    @Override
    public LoyaltyTier calculateTier(long pointsBalance) {
        String tier = oraclePackageExecutor.executeFunction(
                ROUTINE_NAME,
                CALL_SYNTAX,
                1,
                Types.VARCHAR,
                String.class,
                statement -> statement.setLong(2, pointsBalance));
        return LoyaltyTier.fromDatabaseValue(tier);
    }
}
