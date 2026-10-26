package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.domain.InventoryReservation;
import com.ecommerce.inventory.domain.InventoryReservationStatus;
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

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from InventoryReservation r where r.reservationId = :reservationId")
    Optional<InventoryReservation> findLockedById(@Param("reservationId") Long reservationId);

    boolean existsByInventoryIdAndOrderIdAndStatus(
            Long inventoryId,
            Long orderId,
            InventoryReservationStatus status);

    Page<InventoryReservation> findByInventoryId(Long inventoryId, Pageable pageable);

    List<InventoryReservation> findByOrderIdAndStatusIn(
            Long orderId,
            Collection<InventoryReservationStatus> statuses);

    @Query(value = """
            SELECT r.*
              FROM ecomm.inventory_reservations r
             WHERE DECODE(r.status, 'ACTIVE', 1, 0) = 1
               AND NVL(
                       r.expires_at,
                       SYSTIMESTAMP + NUMTODSINTERVAL(365, 'DAY'))
                   <= SYSTIMESTAMP + NUMTODSINTERVAL(:withinMinutes, 'MINUTE')
             ORDER BY NVL(r.expires_at, SYSTIMESTAMP), r.reservation_id
             FETCH FIRST :batchSize ROWS ONLY
            """, nativeQuery = true)
    List<InventoryReservation> findOracleReservationsExpiringWithin(
            @Param("withinMinutes") int withinMinutes,
            @Param("batchSize") int batchSize);
}
