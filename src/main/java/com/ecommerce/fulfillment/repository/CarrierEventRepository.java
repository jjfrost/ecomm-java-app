package com.ecommerce.fulfillment.repository;

import com.ecommerce.fulfillment.model.CarrierEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarrierEventRepository extends JpaRepository<CarrierEvent, Long> {
    List<CarrierEvent> findByShipmentIdOrderByEventTimeAsc(Long shipmentId);

    Optional<CarrierEvent> findFirstByShipmentIdOrderByEventTimeDesc(Long shipmentId);
}
