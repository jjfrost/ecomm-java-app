package com.ecommerce.returns.repository;

import com.ecommerce.returns.model.ReturnDisposition;
import com.ecommerce.returns.model.ReturnItem;
import com.ecommerce.returns.model.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    List<ReturnItem> findByReturnRequestIdOrderByReturnItemId(Long returnRequestId);

    Optional<ReturnItem> findByReturnItemIdAndReturnRequestId(
            Long returnItemId,
            Long returnRequestId);

    long countByReturnRequestIdAndDisposition(
            Long returnRequestId,
            ReturnDisposition disposition);

    @Query("""
            SELECT COUNT(item)
              FROM ReturnItem item, ReturnRequest request
             WHERE item.returnRequestId = request.returnRequestId
               AND item.orderItemId IN :orderItemIds
               AND request.returnStatus IN :statuses
            """)
    long countOpenItems(
            @Param("orderItemIds") Collection<Long> orderItemIds,
            @Param("statuses") Collection<ReturnStatus> statuses);
}
