package com.bank.account.infrastructure.output.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name ="accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 20)
    private String accountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal initialBalance;

    @Column(nullable = false)
    private Boolean status;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;
}