package com.ailab.operations.product;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    private String id;
    private String name;
    private BigDecimal unitPrice;

    protected ProductEntity() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
