package com.ailab.operations.delivery;

import org.springframework.stereotype.Service;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    public DeliveryEntity getByOrderId(String orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException(orderId));
    }
}
