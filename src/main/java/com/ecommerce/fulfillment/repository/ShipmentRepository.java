package com.ecommerce.fulfillment.repository;

import com.ecommerce.fulfillment.model.Shipment;
import com.ecommerce.fulfillment.model.ShipmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByTrackingNumber(String trackingNumber);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    @Query("""
            SELECT shipment
              FROM Shipment shipment
             WHERE (:orderId IS NULL OR shipment.orderId = :orderId)
               AND (:status IS NULL OR shipment.shipmentStatus = :status)
               AND (:carrierCode IS NULL OR UPPER(shipment.carrierCode) = UPPER(:carrierCode))
            """)
    Page<Shipment> search(
            @Param("orderId") Long orderId,
            @Param("status") ShipmentStatus status,
            @Param("carrierCode") String carrierCode,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT shipment FROM Shipment shipment WHERE shipment.shipmentId = :shipmentId")
    Optional<Shipment> findByIdForUpdate(@Param("shipmentId") Long shipmentId);

    @Query(value = """
            SELECT shipment.*
              FROM ecomm.shipments shipment
             WHERE shipment.tracking_number = :trackingNumber
               FOR UPDATE NOWAIT
            """, nativeQuery = true)
    Optional<Shipment> findByTrackingNumberForUpdateNowait(
            @Param("trackingNumber") String trackingNumber);
}
