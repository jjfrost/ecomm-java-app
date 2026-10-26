package com.ecommerce.supplier.repository;

import com.ecommerce.supplier.model.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    boolean existsByReceiptReference(String receiptReference);

    boolean existsByPurchaseOrderId(Long purchaseOrderId);

    List<GoodsReceipt> findByPurchaseOrderIdOrderByReceivedAtAsc(Long purchaseOrderId);
}
