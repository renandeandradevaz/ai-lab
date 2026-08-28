package com.ailab.operations.delivery;

public class DeliveryNotFoundException extends RuntimeException {

    public DeliveryNotFoundException(String orderId) {
        super("Delivery not found for order: " + orderId);
    }
}
