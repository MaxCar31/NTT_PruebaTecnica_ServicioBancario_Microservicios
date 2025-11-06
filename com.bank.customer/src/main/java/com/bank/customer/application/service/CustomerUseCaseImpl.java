package com.bank.customer.application.service;

import com.bank.customer.application.input.port.CustomerUseCase;
import com.bank.customer.application.output.port.CustomerRepositoryPort;
import com.bank.customer.domain.exception.DuplicateResourceException;
import com.bank.customer.domain.exception.ResourceNotFoundException;
import com.bank.customer.domain.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerUseCaseImpl implements CustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Customer> createCustomer(Customer customer) {
        log.info("Attempting to create a customer with identification: {}", customer.getIdentification());
        return validateUniqueIdentification(customer.getIdentification())
                .then(Mono.defer(() -> {
                    String hashedPassword = passwordEncoder.encode(customer.getPassword());
                    customer.setPassword(hashedPassword);
                    customer.setStatus(true);
                    log.info("Validation successful. Proceeding to save new customer.");
                    return customerRepositoryPort.saveCustomer(customer)
                            .doOnSuccess(c -> log.info("Successfully created customer with ID: {}", c.getCustomerId()));
                }));
    }

    /**
     * Valida que no exista otro cliente con la misma identificación.
     * Retorna un Mono<Void> que completa si es válido, o un Mono.error si no lo es.
     */
    private Mono<Void> validateUniqueIdentification(String identification) {
        return customerRepositoryPort.findCustomerByIdentification(identification)
                .flatMap(existingCustomer -> {
                    String errorMsg = "Customer with identification '" + identification + "' already exists.";
                    log.warn(errorMsg);
                    return Mono.error(new DuplicateResourceException(errorMsg));
                }).then();
    }



    @Override
    public Mono<Customer> updateCustomer(Long id, Customer customerUpdateRequest) {
        // Primero, buscamos el cliente que se va a actualizar
        return customerRepositoryPort.findCustomerById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found with id: " + id)))
                .flatMap(existingCustomer -> {

                    // Creamos un Mono para la validación. Si no hay que validar, será un Mono vacío.
                    Mono<Void> validationMono = Mono.empty();

                    // Si la petición de actualización incluye una nueva identificación...
                    if (customerUpdateRequest.getIdentification() != null && !customerUpdateRequest.getIdentification().equals(existingCustomer.getIdentification())) {
                        log.info("Identification change detected for customer ID {}. Validating new identification '{}'", id, customerUpdateRequest.getIdentification());
                        // la validamos para evitar duplicados.
                        validationMono = validateUniqueIdentificationForUpdate(customerUpdateRequest.getIdentification(), id);
                    }

                    // Ejecutamos la validación y LUEGO actualizamos y guardamos.
                    return validationMono.then(Mono.defer(() -> {
                        // Actualizamos los campos del cliente existente
                        existingCustomer.setName(customerUpdateRequest.getName());
                        // ... (otros campos como address, phone, etc.)
                        existingCustomer.setIdentification(customerUpdateRequest.getIdentification());
                        existingCustomer.setStatus(customerUpdateRequest.getStatus());

                        log.info("Updating customer with ID: {}", id);
                        return customerRepositoryPort.saveCustomer(existingCustomer);
                    }));
                });
    }
    private Mono<Void> validateUniqueIdentificationForUpdate(String identification, Long customerId) {
        return customerRepositoryPort.findCustomerByIdentificationAndCustomerIdNot(identification, customerId)
                .flatMap(existingCustomer -> {
                    String errorMsg = "Cannot update. Another customer with identification '" + identification + "' already exists.";
                    log.warn(errorMsg);
                    return Mono.error(new DuplicateResourceException(errorMsg));
                }).then();
    }

    @Override
    public Mono<Customer> findCustomerById(Long id) {
        return customerRepositoryPort.findCustomerById(id);
    }

    @Override
    public Mono<Void> deleteCustomer(Long id) {

        return customerRepositoryPort.findCustomerById(id)
                .flatMap(customerFound -> {
                    customerFound.setStatus(false);
                    return customerRepositoryPort.saveCustomer(customerFound);
                })
                .then();
    }

    @Override
    public Flux<Customer> findAllCustomers() {
        return customerRepositoryPort.findAll();
    }
}