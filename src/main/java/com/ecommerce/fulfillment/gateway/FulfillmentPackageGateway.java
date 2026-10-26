package com.ecommerce.fulfillment.gateway;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.AssignTrackingRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CarrierEventRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.CreateShipmentRequest;
import com.ecommerce.fulfillment.dto.FulfillmentDtos.ShipmentItemRequest;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

@Component
public class FulfillmentPackageGateway {
    private static final String CREATE_SHIPMENT = "ecomm.FULFILLMENT_PKG.create_shipment";
    private static final String ADD_SHIPMENT_ITEM = "ecomm.FULFILLMENT_PKG.add_shipment_item";
    private static final String ASSIGN_TRACKING = "ecomm.FULFILLMENT_PKG.assign_tracking";
    private static final String READY_SHIPMENT = "ecomm.FULFILLMENT_PKG.ready_shipment";
    private static final String DISPATCH_SHIPMENT = "ecomm.FULFILLMENT_PKG.dispatch_shipment";
    private static final String RECORD_CARRIER_EVENT = "ecomm.FULFILLMENT_PKG.record_carrier_event";
    private static final String CANCEL_SHIPMENT = "ecomm.FULFILLMENT_PKG.cancel_shipment";

    private final OraclePackageExecutor packageExecutor;
    private final EntityManager entityManager;

    public FulfillmentPackageGateway(
            OraclePackageExecutor packageExecutor,
            EntityManager entityManager) {
        this.packageExecutor = packageExecutor;
        this.entityManager = entityManager;
    }

    public Long createShipment(CreateShipmentRequest request) {
        BigDecimal result = packageExecutor.executeFunction(
                CREATE_SHIPMENT,
                "{? = call ecomm.FULFILLMENT_PKG.create_shipment(?, ?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, request.orderId());
                    statement.setLong(3, request.warehouseId());
                    statement.setString(4, request.carrierCode());
                    statement.setString(5, request.serviceLevel());
                    statement.setString(6, request.trackingNumber());
                    setTimestamp(statement, 7, request.estimatedDeliveryAt());
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void addShipmentItem(Long shipmentId, ShipmentItemRequest item) {
        packageExecutor.execute(
                ADD_SHIPMENT_ITEM,
                "{call ecomm.FULFILLMENT_PKG.add_shipment_item(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, shipmentId);
                    statement.setLong(2, item.orderItemId());
                    statement.setLong(3, item.productId());
                    statement.setInt(4, item.quantity());
                });
        entityManager.clear();
    }

    public void assignTracking(Long shipmentId, AssignTrackingRequest request) {
        packageExecutor.execute(
                ASSIGN_TRACKING,
                "{call ecomm.FULFILLMENT_PKG.assign_tracking(?, ?, ?)}",
                statement -> {
                    statement.setLong(1, shipmentId);
                    statement.setString(2, request.trackingNumber());
                    setTimestamp(statement, 3, request.estimatedDeliveryAt());
                });
        entityManager.clear();
    }

    public void markReady(Long shipmentId) {
        packageExecutor.execute(
                READY_SHIPMENT,
                "{call ecomm.FULFILLMENT_PKG.ready_shipment(?)}",
                statement -> statement.setLong(1, shipmentId));
        entityManager.clear();
    }

    public void dispatch(Long shipmentId, Instant shippedAt) {
        packageExecutor.execute(
                DISPATCH_SHIPMENT,
                "{call ecomm.FULFILLMENT_PKG.dispatch_shipment(?, ?)}",
                statement -> {
                    statement.setLong(1, shipmentId);
                    statement.setTimestamp(2, Timestamp.from(shippedAt));
                });
        entityManager.clear();
    }

    public Long recordCarrierEvent(Long shipmentId, CarrierEventRequest request) {
        BigDecimal result = packageExecutor.executeFunction(
                RECORD_CARRIER_EVENT,
                "{? = call ecomm.FULFILLMENT_PKG.record_carrier_event(?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, shipmentId);
                    statement.setString(3, request.eventCode().name());
                    statement.setString(4, request.eventDescription());
                    statement.setString(5, request.eventLocation());
                    statement.setTimestamp(6, Timestamp.from(request.eventTime()));
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void cancel(Long shipmentId, String reason) {
        packageExecutor.execute(
                CANCEL_SHIPMENT,
                "{call ecomm.FULFILLMENT_PKG.cancel_shipment(?, ?)}",
                statement -> {
                    statement.setLong(1, shipmentId);
                    statement.setString(2, reason);
                });
        entityManager.clear();
    }

    private void setTimestamp(
            java.sql.CallableStatement statement,
            int index,
            Instant value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(index, Timestamp.from(value));
        }
    }
}
