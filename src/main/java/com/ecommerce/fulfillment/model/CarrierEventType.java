package com.ecommerce.fulfillment.model;

public enum CarrierEventType {
    LABEL_CREATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_EXCEPTION,
    RETURNED_TO_SENDER
}
