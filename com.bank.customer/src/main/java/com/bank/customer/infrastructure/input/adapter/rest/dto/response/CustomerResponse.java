package com.bank.customer.infrastructure.input.adapter.rest.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse {
    private Long customerId;
    private String name;
    private String gender;
    private String identification;
    private String address;
    private String phone;
    private Boolean status;
}