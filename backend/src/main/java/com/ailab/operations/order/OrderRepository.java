package com.ailab.operations.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByCustomer_IdOrderByCreatedAtDesc(String customerId);
}
