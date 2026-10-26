package com.ecommerce.fulfillment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipment_items", schema = "ecomm")
public class ShipmentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipment_item_seq")
    @SequenceGenerator(
            name = "shipment_item_seq",
            sequenceName = "ecomm.shipment_item_seq",
            allocationSize = 1)
    @Column(name = "shipment_item_id")
    private Long shipmentItemId;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    protected ShipmentItem() {
    }

    public Long getShipmentItemId() {
        return shipmentItemId;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
