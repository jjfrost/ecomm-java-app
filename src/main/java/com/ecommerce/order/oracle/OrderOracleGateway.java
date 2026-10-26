package com.ecommerce.order.oracle;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.order.domain.Order;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.service.OrderCommandGateway;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Types;

@Component
public class OrderOracleGateway implements OrderCommandGateway {
    private static final String SUBMIT_ORDER = "ecomm.ORDER_PKG.SUBMIT_ORDER";
    private static final String ADD_ORDER_ITEM = "ecomm.ORDER_PKG.ADD_ORDER_ITEM";
    private static final String UPDATE_STATUS = "ecomm.ORDER_PKG.UPDATE_STATUS";
    private static final String CANCEL_ORDER = "ecomm.ORDER_PKG.CANCEL_ORDER";

    private final OraclePackageExecutor packageExecutor;
    private final EntityManager entityManager;

    public OrderOracleGateway(
            OraclePackageExecutor packageExecutor,
            EntityManager entityManager) {
        this.packageExecutor = packageExecutor;
        this.entityManager = entityManager;
    }

    @Override
    public Long submitOrder(
            Long customerId,
            Long cartId,
            BigDecimal totalAmount,
            String requestedBy) {
        return packageExecutor.executeFunction(
                SUBMIT_ORDER,
                "{? = call ecomm.ORDER_PKG.SUBMIT_ORDER(?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                Long.class,
                statement -> {
                    statement.setLong(2, customerId);
                    statement.setLong(3, cartId);
                    statement.setBigDecimal(4, totalAmount);
                    statement.setString(5, requestedBy);
                });
    }

    @Override
    public void addOrderItem(
            Long orderId,
            Long productId,
            BigDecimal quantity,
            BigDecimal unitPrice) {
        packageExecutor.execute(
                ADD_ORDER_ITEM,
                "{call ecomm.ORDER_PKG.ADD_ORDER_ITEM(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, orderId);
                    statement.setLong(2, productId);
                    statement.setBigDecimal(3, quantity);
                    statement.setBigDecimal(4, unitPrice);
                });
    }

    @Override
    public void updateStatus(
            Order order,
            OrderStatus status,
            String changedBy,
            String notes) {
        packageExecutor.execute(
                UPDATE_STATUS,
                "{call ecomm.ORDER_PKG.UPDATE_STATUS(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, order.getOrderId());
                    statement.setString(2, status.name());
                    statement.setString(3, changedBy);
                    if (notes == null) {
                        statement.setNull(4, Types.VARCHAR);
                    } else {
                        statement.setString(4, notes);
                    }
                });
        entityManager.refresh(order);
    }

    @Override
    public void cancelOrder(Order order, String reason, String changedBy) {
        packageExecutor.execute(
                CANCEL_ORDER,
                "{call ecomm.ORDER_PKG.CANCEL_ORDER(?, ?, ?)}",
                statement -> {
                    statement.setLong(1, order.getOrderId());
                    statement.setString(2, reason);
                    statement.setString(3, changedBy);
                });
        entityManager.refresh(order);
    }
}
