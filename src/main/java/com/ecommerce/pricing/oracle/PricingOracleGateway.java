package com.ecommerce.pricing.oracle;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.pricing.service.PricingEngineGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

@Component
public class PricingOracleGateway implements PricingEngineGateway {
    private static final String EFFECTIVE_PRICE = "ecomm.PRICING_PKG.GET_EFFECTIVE_PRICE";
    private static final String VALIDATE_COUPON = "ecomm.PRICING_PKG.VALIDATE_COUPON";

    private final OraclePackageExecutor packageExecutor;

    public PricingOracleGateway(OraclePackageExecutor packageExecutor) {
        this.packageExecutor = packageExecutor;
    }

    @Override
    public BigDecimal getEffectivePrice(
            Long productId,
            BigDecimal quantity,
            Long customerId,
            String couponCode,
            Instant asOf) {
        return packageExecutor.executeFunction(
                EFFECTIVE_PRICE,
                "{? = call ecomm.PRICING_PKG.GET_EFFECTIVE_PRICE(?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, productId);
                    statement.setBigDecimal(3, quantity);
                    if (customerId == null) {
                        statement.setNull(4, Types.NUMERIC);
                    } else {
                        statement.setLong(4, customerId);
                    }
                    if (couponCode == null) {
                        statement.setNull(5, Types.VARCHAR);
                    } else {
                        statement.setString(5, couponCode);
                    }
                    statement.setTimestamp(6, Timestamp.from(asOf));
                });
    }

    @Override
    public boolean validateCoupon(
            String couponCode,
            Long customerId,
            BigDecimal subtotal,
            Instant asOf) {
        Integer result = packageExecutor.executeFunction(
                VALIDATE_COUPON,
                "{? = call ecomm.PRICING_PKG.VALIDATE_COUPON(?, ?, ?, ?)}",
                1,
                Types.INTEGER,
                Integer.class,
                statement -> {
                    statement.setString(2, couponCode);
                    statement.setLong(3, customerId);
                    statement.setBigDecimal(4, subtotal);
                    statement.setTimestamp(5, Timestamp.from(asOf));
                });
        return result != null && result == 1;
    }
}
