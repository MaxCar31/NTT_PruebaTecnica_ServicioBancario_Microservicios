package com.bank.customer.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Person {
    private String name;
    private String gender;
    private String identification;
    private String address;
    private String phone;
}