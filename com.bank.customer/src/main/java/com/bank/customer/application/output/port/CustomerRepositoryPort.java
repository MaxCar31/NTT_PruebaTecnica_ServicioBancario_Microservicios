package com.bank.customer.application.output.port;

import com.bank.customer.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepositoryPort {
    /**
     * Saves (creates or updates) a customer in the database.
     * @param customer The customer to save.
     * @return A Mono that emits the saved customer.
     */
    Mono<Customer> saveCustomer(Customer customer);

    /**
     * Finds a customer by their ID.
     * @param customerId The ID of the customer to find.
     * @return A Mono that emits the found customer, or an empty Mono if it does not exist.
     */
    Mono<Customer> findCustomerById(Long customerId);

    /**
     * Deletes a customer by their ID.
     * @param id The ID of the customer to delete.
     * @return A Mono<Void> that completes when the operation is finished.
     */
    Mono<Void> deleteCustomerById(Long id);
    /**
     * Finds a customer by their identification number.
     * @param identification The identification number to search for.
     * @return A Mono that emits the found customer, or an empty Mono if it does not exist.
     */
    Mono<Customer> findCustomerByIdentification(String identification);

    /**
     * Finds a customer by their identification number, excluding a specific customer ID.
     * This is useful for validating uniqueness during updates.
     * @param identification The identification number to search for.
     * @param customerId The customer ID to exclude from the search.
     * @return A Mono that emits the found customer, or an empty Mono if it does not exist.
     */
    Mono<Customer> findCustomerByIdentificationAndCustomerIdNot(String identification, Long customerId);
    ;
    /**
     * Finds all customers.
     * @return A Flux that emits all customers in the database.
     */
    Flux<Customer> findAll();
}