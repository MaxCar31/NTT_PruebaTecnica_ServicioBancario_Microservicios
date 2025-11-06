package com.bank.account.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing a Customer.
 * This model is used to hold customer data retrieved from the customer-service,
 * it is NOT an entity in this service's database.
 */
@Data
@Builder
public class Customer {
    private Long customerId;
    private String name;
}