package com.ecommerce.returns.gateway;

import com.ecommerce.common.oracle.OraclePackageExecutor;
import com.ecommerce.returns.dto.ReturnDtos.AuthorizeReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.CreateReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.ReceiveReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.ReturnItemRequest;
import com.ecommerce.returns.dto.ReturnDtos.SetDispositionRequest;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;

@Component
public class ReturnPackageGateway {
    private static final String CREATE_RETURN = "ecomm.RETURN_PKG.create_return";
    private static final String ADD_RETURN_ITEM = "ecomm.RETURN_PKG.add_return_item";
    private static final String AUTHORIZE_RETURN = "ecomm.RETURN_PKG.authorize_return";
    private static final String RECEIVE_RETURN = "ecomm.RETURN_PKG.receive_return";
    private static final String SET_DISPOSITION = "ecomm.RETURN_PKG.set_item_disposition";
    private static final String COMPLETE_RETURN = "ecomm.RETURN_PKG.complete_return";
    private static final String CANCEL_RETURN = "ecomm.RETURN_PKG.cancel_return";

    private final OraclePackageExecutor packageExecutor;
    private final EntityManager entityManager;

    public ReturnPackageGateway(
            OraclePackageExecutor packageExecutor,
            EntityManager entityManager) {
        this.packageExecutor = packageExecutor;
        this.entityManager = entityManager;
    }

    public Long createReturn(CreateReturnRequest request) {
        BigDecimal result = packageExecutor.executeFunction(
                CREATE_RETURN,
                "{? = call ecomm.RETURN_PKG.create_return(?, ?, ?, ?)}",
                1,
                Types.NUMERIC,
                BigDecimal.class,
                statement -> {
                    statement.setLong(2, request.orderId());
                    statement.setLong(3, request.customerId());
                    statement.setString(4, request.reason().name());
                    statement.setString(5, request.customerNotes());
                });
        entityManager.clear();
        return result.longValueExact();
    }

    public void addReturnItem(Long returnRequestId, ReturnItemRequest item) {
        packageExecutor.execute(
                ADD_RETURN_ITEM,
                "{call ecomm.RETURN_PKG.add_return_item(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, returnRequestId);
                    statement.setLong(2, item.orderItemId());
                    statement.setLong(3, item.productId());
                    statement.setInt(4, item.quantity());
                });
        entityManager.clear();
    }

    public void authorize(Long returnRequestId, AuthorizeReturnRequest request) {
        packageExecutor.execute(
                AUTHORIZE_RETURN,
                "{call ecomm.RETURN_PKG.authorize_return(?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, returnRequestId);
                    statement.setString(2, request.approved() ? "Y" : "N");
                    statement.setString(3, request.reviewedBy());
                    statement.setString(4, request.reviewNotes());
                });
        entityManager.clear();
    }

    public void receive(Long returnRequestId, ReceiveReturnRequest request) {
        packageExecutor.execute(
                RECEIVE_RETURN,
                "{call ecomm.RETURN_PKG.receive_return(?, ?, ?)}",
                statement -> {
                    statement.setLong(1, returnRequestId);
                    statement.setTimestamp(2, Timestamp.from(request.receivedAt()));
                    statement.setString(3, request.receivedBy());
                });
        entityManager.clear();
    }

    public void setDisposition(Long returnRequestId, Long returnItemId, SetDispositionRequest request) {
        packageExecutor.execute(
                SET_DISPOSITION,
                "{call ecomm.RETURN_PKG.set_item_disposition(?, ?, ?, ?, ?)}",
                statement -> {
                    statement.setLong(1, returnRequestId);
                    statement.setLong(2, returnItemId);
                    statement.setString(3, request.disposition().name());
                    statement.setString(4, request.itemCondition());
                    statement.setBigDecimal(5, request.refundAmount());
                });
        entityManager.clear();
    }

    public void complete(Long returnRequestId) {
        packageExecutor.execute(
                COMPLETE_RETURN,
                "{call ecomm.RETURN_PKG.complete_return(?)}",
                statement -> statement.setLong(1, returnRequestId));
        entityManager.clear();
    }

    public void cancel(Long returnRequestId, String reason) {
        packageExecutor.execute(
                CANCEL_RETURN,
                "{call ecomm.RETURN_PKG.cancel_return(?, ?)}",
                statement -> {
                    statement.setLong(1, returnRequestId);
                    statement.setString(2, reason);
                });
        entityManager.clear();
    }
}
