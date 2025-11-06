package com.bank.account.infrastructure.output.adapter.client;

import com.bank.account.application.output.port.CustomerClientPort;
import com.bank.account.domain.exception.CustomerServiceException;
import com.bank.account.domain.model.Customer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerClientAdapter implements CustomerClientPort {

    private final WebClient customerWebClient;

    private static final String CUSTOMER_SERVICE_CB = "customerService";

    @Override
    @CircuitBreaker(name = CUSTOMER_SERVICE_CB, fallbackMethod = "findCustomerByIdFallback")
    public Mono<Customer> findCustomerById(Long customerId) {
        log.debug("Attempting to find customer by ID: {}", customerId);

        return customerWebClient.get()
                .uri("/api/v1/customers/{id}", customerId)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new CustomerServiceException("Customer not found (404)", null)))

                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new CustomerServiceException("Customer service internal error (5XX)", null)))
                .bodyToMono(Customer.class)
                .timeout(Duration.ofSeconds(3))
                .doOnError(ex -> log.warn("Error calling customer service for ID {}: {}", customerId, ex.getMessage()));
    }

    private Mono<Customer> findCustomerByIdFallback(Long customerId, Exception ex) {
        log.error("Circuit breaker activated or fallback triggered for customer ID: {}. Reason: {}",
                customerId, ex.getMessage());

        return Mono.error(new CustomerServiceException(
                "Customer service is currently unavailable. Circuit breaker may be OPEN.", ex));
    }
}