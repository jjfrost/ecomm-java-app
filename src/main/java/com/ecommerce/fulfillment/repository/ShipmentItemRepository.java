package com.ecommerce.fulfillment.repository;

import com.ecommerce.fulfillment.model.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {
    boolean existsByOrderItemIdIn(Collection<Long> orderItemIds);

    List<ShipmentItem> findByShipmentIdOrderByShipmentItemId(Long shipmentId);
}
