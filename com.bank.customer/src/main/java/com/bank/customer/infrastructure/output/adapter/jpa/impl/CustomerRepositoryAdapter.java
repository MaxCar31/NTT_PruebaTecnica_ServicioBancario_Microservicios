package com.bank.customer.infrastructure.output.adapter.jpa.impl;

import com.bank.customer.application.output.port.CustomerRepositoryPort;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.infrastructure.output.adapter.jpa.entity.CustomerEntity;
import com.bank.customer.infrastructure.output.adapter.jpa.mapper.CustomerJpaMapper;
import com.bank.customer.infrastructure.output.adapter.jpa.repository.CustomerSpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final CustomerSpringRepository customerRepository;
    private final CustomerJpaMapper customerMapper;

    @Override
    public Mono<Customer> saveCustomer(Customer customer) {
        return Mono.fromCallable(() -> {
            CustomerEntity customerEntity = customerMapper.toEntity(customer);
            return customerMapper.toDomain(customerRepository.save(customerEntity));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Customer> findCustomerById(Long customerId) {
        return Mono.fromCallable(() -> customerRepository.findById(customerId)
                        .map(customerMapper::toDomain))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Customer> findCustomerByIdentification(String identification) {
        return Mono.fromCallable(() -> customerRepository.findByIdentification(identification)
                        .map(customerMapper::toDomain))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteCustomerById(Long id) {
        return Mono.fromRunnable(() -> customerRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Customer> findCustomerByIdentificationAndCustomerIdNot(String identification, Long customerId) {
        return Mono.fromCallable(() -> customerRepository.findByIdentificationAndCustomerIdNot(identification, customerId)
                        .map(customerMapper::toDomain))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Customer> findAll() {
        return Flux.fromIterable(customerRepository.findAllByStatusIsTrue())
                .map(customerMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }
}