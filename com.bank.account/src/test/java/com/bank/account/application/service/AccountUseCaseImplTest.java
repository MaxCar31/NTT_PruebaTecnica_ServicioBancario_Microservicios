package com.bank.account.application.service;

import com.bank.account.application.output.port.AccountRepositoryPort;
import com.bank.account.application.output.port.CustomerClientPort;
import com.bank.account.domain.exception.DuplicateResourceException;
import com.bank.account.domain.exception.ResourceNotFoundException;
import com.bank.account.domain.model.Account;
import com.bank.account.domain.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Use Case Unit Tests")
class AccountUseCaseImplTest {

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private CustomerClientPort customerClientPort; // <-- CAMBIO IMPORTANTE

    @InjectMocks
    private AccountUseCaseImpl accountUseCaseImpl;

    private Account testAccount;
    private Customer testCustomer; // <-- CAMBIO (ahora es el DTO)

    @BeforeEach
    void setUp() {
        // Este es el DTO simple del 'account-service'
        testCustomer = Customer.builder().customerId(1L).name("Jose Lema").build();
        
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("478758")
                .accountType("Savings")
                .initialBalance(BigDecimal.valueOf(2000.00))
                .status(true)
                .customerId(1L)
                .build();
    }

    @Test
    @DisplayName("should create a new account successfully")
    void shouldCreateAccountSuccessfully() {
        // Arrange
        when(accountRepositoryPort.findAccountByNumber(any(String.class))).thenReturn(Mono.empty());
        // Simula la llamada exitosa al microservicio de clientes
        when(customerClientPort.findCustomerById(testCustomer.getCustomerId())).thenReturn(Mono.just(testCustomer)); // <-- CAMBIO IMPORTANTE
        when(accountRepositoryPort.saveAccount(any(Account.class))).thenReturn(Mono.just(testAccount));

        // Act & Assert
        StepVerifier.create(accountUseCaseImpl.createAccount(testAccount))
                .expectNext(testAccount)
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail to create account if customer does not exist")
    void shouldFailCreateAccountIfCustomerDoesNotExist() {
        // Arrange
        when(accountRepositoryPort.findAccountByNumber(any(String.class))).thenReturn(Mono.empty());
        // Simula que el microservicio de clientes no encuentra al cliente
        when(customerClientPort.findCustomerById(any(Long.class))).thenReturn(Mono.empty()); // <-- CAMBIO IMPORTANTE

        // Act & Assert
        StepVerifier.create(accountUseCaseImpl.createAccount(testAccount))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should find an account by ID successfully")
    void shouldFindAccountByIdSuccessfully() {
        when(accountRepositoryPort.findAccountById(1L)).thenReturn(Mono.just(testAccount));
        StepVerifier.create(accountUseCaseImpl.findAccountById(1L))
                .expectNext(testAccount)
                .verifyComplete();
    }

    @Test
    @DisplayName("should find an account by account number successfully")
    void shouldFindAccountByNumberSuccessfully() {
        when(accountRepositoryPort.findAccountByNumber("478758")).thenReturn(Mono.just(testAccount));
        StepVerifier.create(accountUseCaseImpl.findAccountByNumber("478758"))
                .expectNext(testAccount)
                .verifyComplete();
    }

    @Test
    @DisplayName("should update an existing account successfully")
    void shouldUpdateAccountSuccessfully() {
        Account updatedAccount = testAccount.toBuilder().accountType("Checking").build();
        when(accountRepositoryPort.findAccountById(1L)).thenReturn(Mono.just(testAccount));
        when(accountRepositoryPort.saveAccount(any(Account.class))).thenReturn(Mono.just(updatedAccount));

        StepVerifier.create(accountUseCaseImpl.updateAccount(1L, updatedAccount))
                .expectNext(updatedAccount)
                .verifyComplete();
    }

    @Test
    @DisplayName("should logically delete an account by setting status to false")
    void shouldLogicallyDeleteAccount() {
        Account deactivatedAccount = testAccount.toBuilder().status(false).build();
        when(accountRepositoryPort.findAccountById(1L)).thenReturn(Mono.just(testAccount));
        when(accountRepositoryPort.saveAccount(any(Account.class))).thenReturn(Mono.just(deactivatedAccount));

        StepVerifier.create(accountUseCaseImpl.deleteAccount(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("should throw DuplicateResourceException when creating with existing account number")
    void shouldThrowExceptionWhenCreatingWithExistingAccountNumber() {
        // Arrange
        when(accountRepositoryPort.findAccountByNumber("478758")).thenReturn(Mono.just(testAccount));
        // Simula la llamada al cliente (aunque fallará antes, es buena práctica)
        when(customerClientPort.findCustomerById(any(Long.class))).thenReturn(Mono.just(testCustomer)); // <-- CAMBIO IMPORTANTE

        // Act
        Mono<Account> result = accountUseCaseImpl.createAccount(testAccount);
        
        // Assert
        StepVerifier.create(result)
                .expectError(DuplicateResourceException.class)
                .verify();
    }

    @Test
    @DisplayName("should throw DuplicateResourceException when updating to an existing account number")
    void shouldThrowExceptionWhenUpdatingToExistingAccountNumber() {
        Account accountToUpdate = Account.builder().id(1L).accountNumber("111").build();
        Account otherAccount = Account.builder().id(2L).accountNumber("222").build();
        Account updateRequest = Account.builder().accountNumber("222").accountType("Ahorro").status(true).build();

        when(accountRepositoryPort.findAccountById(1L)).thenReturn(Mono.just(accountToUpdate));
        when(accountRepositoryPort.findAccountByAccountNumberAndIdNot("222", 1L)).thenReturn(Mono.just(otherAccount));

        Mono<Account> result = accountUseCaseImpl.updateAccount(1L, updateRequest);
        StepVerifier.create(result)
                .expectError(DuplicateResourceException.class)
                .verify();
    }
}