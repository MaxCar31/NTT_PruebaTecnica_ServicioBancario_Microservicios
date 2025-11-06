package com.bank.customer.infrastructure.output.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customers")
public class CustomerEntity extends PersonEntity {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean status;
}