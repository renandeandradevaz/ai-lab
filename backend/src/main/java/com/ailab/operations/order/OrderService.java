package com.ailab.operations.order;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderEntity getById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<OrderEntity> getByCustomerId(String customerId) {
        return orderRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId);
    }
}
