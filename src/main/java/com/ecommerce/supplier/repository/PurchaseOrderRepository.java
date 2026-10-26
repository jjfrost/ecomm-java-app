package com.ecommerce.supplier.repository;

import com.ecommerce.supplier.model.PurchaseOrder;
import com.ecommerce.supplier.model.PurchaseOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    boolean existsBySupplierIdAndPurchaseOrderStatusIn(
            Long supplierId,
            Collection<PurchaseOrderStatus> statuses);

    @Query("""
            SELECT purchaseOrder
              FROM PurchaseOrder purchaseOrder
             WHERE (:supplierId IS NULL OR purchaseOrder.supplierId = :supplierId)
               AND (:warehouseId IS NULL OR purchaseOrder.warehouseId = :warehouseId)
               AND (:status IS NULL OR purchaseOrder.purchaseOrderStatus = :status)
            """)
    Page<PurchaseOrder> search(
            @Param("supplierId") Long supplierId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") PurchaseOrderStatus status,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT purchaseOrder
              FROM PurchaseOrder purchaseOrder
             WHERE purchaseOrder.purchaseOrderId = :purchaseOrderId
            """)
    Optional<PurchaseOrder> findByIdForUpdate(
            @Param("purchaseOrderId") Long purchaseOrderId);

    @Query(value = """
            SELECT purchase_order.*
              FROM ecomm.purchase_orders purchase_order
             WHERE DECODE(
                       purchase_order.purchase_order_status,
                       'APPROVED', 1,
                       'PARTIALLY_RECEIVED', 1,
                       0) = 1
               AND TRUNC(purchase_order.expected_date)
                       < TRUNC(SYSDATE) - :minimumDaysOverdue
             ORDER BY purchase_order.expected_date,
                      purchase_order.purchase_order_id
             FETCH FIRST :maxRows ROWS ONLY
            """, nativeQuery = true)
    List<PurchaseOrder> findOverdueApprovedOrders(
            @Param("minimumDaysOverdue") int minimumDaysOverdue,
            @Param("maxRows") int maxRows);
}
