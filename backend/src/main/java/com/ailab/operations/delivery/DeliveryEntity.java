package com.ailab.operations.delivery;

import java.time.Instant;
import java.time.LocalDate;
import com.ailab.operations.order.OrderEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "deliveries")
public class DeliveryEntity {

    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    private LocalDate estimatedDeliveryDate;
    private Instant deliveredAt;

    protected DeliveryEntity() {
    }

    public String getId() {
        return id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}
