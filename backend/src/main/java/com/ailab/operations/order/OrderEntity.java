package com.ailab.operations.order;

import java.math.BigDecimal;
import java.time.Instant;
import com.ailab.operations.customer.CustomerEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Instant createdAt;
    private BigDecimal totalAmount;

    protected OrderEntity() {
    }

    public String getId() {
        return id;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
