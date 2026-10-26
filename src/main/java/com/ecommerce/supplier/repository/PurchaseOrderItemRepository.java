package com.ecommerce.supplier.repository;

import com.ecommerce.supplier.model.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
    List<PurchaseOrderItem> findByPurchaseOrderIdOrderByPurchaseOrderItemId(Long purchaseOrderId);

    Optional<PurchaseOrderItem> findByPurchaseOrderItemIdAndPurchaseOrderId(
            Long purchaseOrderItemId,
            Long purchaseOrderId);

    long countByPurchaseOrderId(Long purchaseOrderId);
}
