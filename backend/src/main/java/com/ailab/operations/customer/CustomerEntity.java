package com.ailab.operations.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    private String id;
    private String fullName;
    private String email;

    protected CustomerEntity() {
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
