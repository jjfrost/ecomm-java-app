package com.ecommerce.supplier.gateway;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.supplier.dto.SupplierDtos.CreatePurchaseOrderRequest;
import com.ecommerce.supplier.dto.SupplierDtos.ReceiveGoodsRequest;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;

@Component
public class ProcurementPackageGateway {
    private static final String CREATE_PURCHASE_ORDER =
            "ecomm.PROCUREMENT_PKG.create_purchase_order";
    private static final String ADD_PURCHASE_ORDER_ITEM =
            "ecomm.PROCUREMENT_PKG.add_purchase_order_item";
    private static final String SUBMIT_PURCHASE_ORDER =
            "ecomm.PROCUREMENT_PKG.submit_purchase_order";
    private static final String APPROVE_PURCHASE_ORDER =
            "ecomm.PROCUREMENT_PKG.approve_purchase_order";
    private static final String RECEIVE_GOODS = "ecomm.PROCUREMENT_PKG.receive_goods";
    private static final String CANCEL_PURCHASE_ORDER =
            "ecomm.PROCUREMENT_PKG.cancel_purchase_order";
    private static final String CLOSE_PURCHASE_ORDER =
            "ecomm.PROCUREMENT_PKG.close_purchase_order";

    private final OraclePackageExecutor packageExecutor;
    private final EntityManager entityManager;

    public ProcurementPackageGateway(
            OraclePackageExecutor packageExecutor,
            EntityManager entityManager) {
        this.packageExecutor = packageExecutor;
        this.entityManager = entityManager;
    }

    public Long createPurchaseOrder(CreatePurchaseOrderRequest request, String createdBy) {
        BigDecimal result = packageExecutor.executeFunction(
                CREATE_PURCHASE_ORDER,
                "{? = call ecomm.PROCUREMENT_PKG.create_purchase_order(?, ?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, request.supplierId());
                    statement.setLong(3, request.warehouseId());
                    statement.setDate(4, java.sql.Date.valueOf(request.expectedDate()));
                    statement.setBigDecimal(5, request.taxAmount());
                    statement.setBigDecimal(6, request.shippingAmount());
                    statement.setString(7, createdBy);
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void addPurchaseOrderItem(
            Long purchaseOrderId,
            Long supplierProductId,
            Integer quantity) {
        packageExecutor.execute(
                ADD_PURCHASE_ORDER_ITEM,
                "{call ecomm.PROCUREMENT_PKG.add_purchase_order_item(?, ?, ?)}",
                statement -> {
                    statement.setLong(1, purchaseOrderId);
                    statement.setLong(2, supplierProductId);
                    statement.setInt(3, quantity);
                });
        entityManager.clear();
    }

    public void submit(Long purchaseOrderId) {
        packageExecutor.execute(
                SUBMIT_PURCHASE_ORDER,
                "{call ecomm.PROCUREMENT_PKG.submit_purchase_order(?)}",
                statement -> statement.setLong(1, purchaseOrderId));
        entityManager.clear();
    }

    public void approve(Long purchaseOrderId, String approvedBy) {
        packageExecutor.execute(
                APPROVE_PURCHASE_ORDER,
                "{call ecomm.PROCUREMENT_PKG.approve_purchase_order(?, ?)}",
                statement -> {
                    statement.setLong(1, purchaseOrderId);
                    statement.setString(2, approvedBy);
                });
        entityManager.clear();
    }

    public Long receiveGoods(Long purchaseOrderId, ReceiveGoodsRequest request) {
        BigDecimal result = packageExecutor.executeFunction(
                RECEIVE_GOODS,
                "{? = call ecomm.PROCUREMENT_PKG.receive_goods(?, ?, ?, ?, ?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, purchaseOrderId);
                    statement.setLong(3, request.purchaseOrderItemId());
                    statement.setInt(4, request.receivedQuantity());
                    statement.setInt(5, request.rejectedQuantity());
                    statement.setString(6, request.receiptReference());
                    statement.setString(7, request.receivedBy());
                    statement.setTimestamp(8, Timestamp.from(request.receivedAt()));
                    statement.setString(9, request.notes());
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void cancel(Long purchaseOrderId, String reason) {
        packageExecutor.execute(
                CANCEL_PURCHASE_ORDER,
                "{call ecomm.PROCUREMENT_PKG.cancel_purchase_order(?, ?)}",
                statement -> {
                    statement.setLong(1, purchaseOrderId);
                    statement.setString(2, reason);
                });
        entityManager.clear();
    }

    public void close(Long purchaseOrderId) {
        packageExecutor.execute(
                CLOSE_PURCHASE_ORDER,
                "{call ecomm.PROCUREMENT_PKG.close_purchase_order(?)}",
                statement -> statement.setLong(1, purchaseOrderId));
        entityManager.clear();
    }
}
