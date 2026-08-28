package com.ailab.operations.delivery;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<DeliveryEntity, String> {

    Optional<DeliveryEntity> findByOrderId(String orderId);
}
