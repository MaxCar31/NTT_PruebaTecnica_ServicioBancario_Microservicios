package com.bank.customer.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data @NoArgsConstructor @SuperBuilder(toBuilder = true) @EqualsAndHashCode(callSuper = true)
public class Customer extends Person {
    private Long customerId;
    private String password;
    private Boolean status;
}