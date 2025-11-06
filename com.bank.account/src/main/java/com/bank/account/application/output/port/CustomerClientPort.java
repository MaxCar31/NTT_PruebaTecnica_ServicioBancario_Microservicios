package com.bank.account.application.output.port;

import com.bank.account.domain.model.Customer;
import reactor.core.publisher.Mono;

/**
 * Output port for communicating with the Customer microservice.
 * This defines the contract for fetching customer data from an external source.
 */
public interface CustomerClientPort {
    /**
     * Finds a customer by their ID.
     * @param customerId The ID of the customer to find.
     * @return A Mono that emits the found customer, or an error if not found or service is unavailable.
     */
    Mono<Customer> findCustomerById(Long customerId);
}