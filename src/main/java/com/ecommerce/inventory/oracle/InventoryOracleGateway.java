package com.ecommerce.inventory.oracle;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.inventory.domain.Inventory;
import com.ecommerce.inventory.domain.InventoryReservation;
import com.ecommerce.inventory.service.InventoryCommandGateway;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

@Component
public class InventoryOracleGateway implements InventoryCommandGateway {
    private static final String RECEIVE_STOCK = "ecomm.INVENTORY_PKG.RECEIVE_STOCK";
    private static final String ADJUST_STOCK = "ecomm.INVENTORY_PKG.ADJUST_STOCK";
    private static final String RESERVE_STOCK = "ecomm.INVENTORY_PKG.RESERVE_STOCK";
    private static final String RELEASE_STOCK = "ecomm.INVENTORY_PKG.RELEASE_STOCK";

    private final OraclePackageExecutor packageExecutor;
    private final EntityManager entityManager;

    public InventoryOracleGateway(
            OraclePackageExecutor packageExecutor,
            EntityManager entityManager) {
        this.packageExecutor = packageExecutor;
        this.entityManager = entityManager;
    }

    @Override
    public void receiveStock(
            Inventory inventory,
            BigDecimal quantity,
            String reference,
            String performedBy) {
        packageExecutor.execute(
                RECEIVE_STOCK,
                "{call ecomm.INVENTORY_PKG.RECEIVE_STOCK(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, inventory.getInventoryId());
                    statement.setBigDecimal(2, quantity);
                    statement.setString(3, reference);
                    statement.setString(4, performedBy);
                });
        entityManager.refresh(inventory);
    }

    @Override
    public void adjustStock(
            Inventory inventory,
            BigDecimal quantityDelta,
            String reason,
            String performedBy) {
        packageExecutor.execute(
                ADJUST_STOCK,
                "{call ecomm.INVENTORY_PKG.ADJUST_STOCK(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, inventory.getInventoryId());
                    statement.setBigDecimal(2, quantityDelta);
                    statement.setString(3, reason);
                    statement.setString(4, performedBy);
                });
        entityManager.refresh(inventory);
    }

    @Override
    public Long reserveStock(
            Inventory inventory,
            Long orderId,
            BigDecimal quantity,
            Instant expiresAt,
            String performedBy) {
        Long reservationId = packageExecutor.executeFunction(
                RESERVE_STOCK,
                "{? = call ecomm.INVENTORY_PKG.RESERVE_STOCK(?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                Long.class,
                statement -> {
                    statement.setLong(2, inventory.getInventoryId());
                    statement.setLong(3, orderId);
                    statement.setBigDecimal(4, quantity);
                    statement.setTimestamp(5, Timestamp.from(expiresAt));
                    statement.setString(6, performedBy);
                });
        entityManager.refresh(inventory);
        return reservationId;
    }

    @Override
    public void releaseStock(
            InventoryReservation reservation,
            String reason,
            String performedBy) {
        packageExecutor.execute(
                RELEASE_STOCK,
                "{call ecomm.INVENTORY_PKG.RELEASE_STOCK(?, ?, ?)}",
                statement -> {
                    statement.setLong(1, reservation.getReservationId());
                    statement.setString(2, reason);
                    statement.setString(3, performedBy);
                });
        entityManager.refresh(reservation);
    }
}
