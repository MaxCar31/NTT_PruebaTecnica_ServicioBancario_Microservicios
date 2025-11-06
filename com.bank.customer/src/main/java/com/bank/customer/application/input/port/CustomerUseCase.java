package com.bank.customer.application.input.port;

import com.bank.customer.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerUseCase {
    /**
     * Creates a new customer.
     * @param customer The customer to create.
     * @return A Mono that emits the created customer.
     */
    Mono<Customer> createCustomer(Customer customer);

    /**
     * Updates an existing customer.
     * @param id The ID of the customer to update.
     * @param customer The updated customer data.
     * @return A Mono that emits the updated customer, or an empty Mono if not found.
     */
    Mono<Customer> updateCustomer(Long id, Customer customer);

    /**
     * Finds a customer by their ID.
     * @param id The ID of the customer to find.
     * @return A Mono that emits the found customer, or an empty Mono if it does not exist.
     */
    Mono<Customer> findCustomerById(Long id);

    /**
     * Deletes a customer by their ID.
     * @param id The ID of the customer to delete.
     * @return A Mono<Void> that completes when the operation is finished.
     */
    Mono<Void> deleteCustomer(Long id);
    /**
     * Finds all registered customers.
     * @return A Flux that emits all customers.
     */
    Flux<Customer> findAllCustomers();
}