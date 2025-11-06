package com.bank.customer.infrastructure.output.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class PersonEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String gender;

    @Column(unique = true, nullable = false, length = 15)
    private String identification;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 15)
    private String phone;
}