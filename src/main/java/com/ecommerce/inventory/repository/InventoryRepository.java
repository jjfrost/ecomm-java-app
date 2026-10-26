package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.inventoryId = :inventoryId")
    Optional<Inventory> findLockedById(@Param("inventoryId") Long inventoryId);

    Optional<Inventory> findByProductIdAndLocationIgnoreCase(Long productId, String location);

    Page<Inventory> findByProductId(Long productId, Pageable pageable);

    Page<Inventory> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select i
              from Inventory i
             where i.productId = :productId
               and (i.quantityOnHand - i.quantityReserved) >= :quantity
             order by (i.quantityOnHand - i.quantityReserved) desc
            """)
    List<Inventory> findReservableInventory(
            @Param("productId") Long productId,
            @Param("quantity") BigDecimal quantity,
            Pageable pageable);

    @Query("""
            select i
              from Inventory i
             where (i.quantityOnHand - i.quantityReserved) <= i.reorderLevel
            """)
    Page<Inventory> findReorderCandidates(Pageable pageable);

    @Query(value = """
            SELECT i.*
              FROM ecomm.inventory i
             WHERE (NVL(i.quantity_on_hand, 0) - NVL(i.quantity_reserved, 0))
                       <= NVL(i.reorder_level, 0)
               AND (:locationToken IS NULL
                    OR INSTR(UPPER(NVL(i.location, '~')), UPPER(:locationToken)) > 0)
               AND ROWNUM <= :batchSize
             ORDER BY NVL(i.last_updated, i.created_at), i.inventory_id
             FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Inventory> lockOracleReplenishmentBatch(
            @Param("locationToken") String locationToken,
            @Param("batchSize") int batchSize);
}
