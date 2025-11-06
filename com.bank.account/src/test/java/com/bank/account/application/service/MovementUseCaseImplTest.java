package com.bank.account.application.service;

import com.bank.account.application.output.port.AccountRepositoryPort;
import com.bank.account.application.output.port.MovementRepositoryPort;
import com.bank.account.domain.exception.ResourceNotFoundException;
import com.bank.account.domain.model.Account;
import com.bank.account.domain.model.Movement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("Movement Use Case Unit Tests")
class MovementUseCaseImplTest {

    @Mock
    private MovementRepositoryPort movementRepositoryPort;
    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private PlatformTransactionManager transactionManager;
    
    @InjectMocks
    private MovementUseCaseImpl movementUseCaseImpl;

    private Account testAccount;
    private Movement testMovementCredit;
    private Movement testMovementDebit;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .accountNumber("478758")
                .initialBalance(new BigDecimal("1000.00"))
                .status(true)
                .customerId(1L)
                .build();
        
        testMovementCredit = Movement.builder()
                .accountId(1L)
                .amount(new BigDecimal("500.00"))
                .build();
        
        testMovementDebit = Movement.builder()
                .accountId(1L)
                .amount(new BigDecimal("-200.00"))
                .build();
    }

    // Nota: El test de "registerMovement" es complejo de probar unitariamente
    // debido al uso de TransactionTemplate. En el monolito no estaba testeado
    // el flujo transaccional. Para este microservicio, nos enfocaremos
    // en los otros métodos que sí son puramente reactivos.
    // El testeo del registro se delega a la Prueba de Integración (Fase 6.3).


    @Test
    @DisplayName("should fail to register a movement with an amount of zero")
    void shouldFailRegisterMovementWithZeroAmount() {
        // Arrange
        Movement zeroAmountMovement = Movement.builder()
                .accountId(1L)
                .amount(BigDecimal.ZERO)
                .build();
        
        // Act & Assert
        StepVerifier.create(movementUseCaseImpl.registerMovement(zeroAmountMovement))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("should find a movement by ID successfully")
    void shouldFindMovementByIdSuccessfully() {
        // Arrange
        when(movementRepositoryPort.findById(any(Long.class))).thenReturn(Mono.just(testMovementCredit));
        
        // Act & Assert
        StepVerifier.create(movementUseCaseImpl.findMovementById(1L))
                .expectNext(testMovementCredit)
                .verifyComplete();
    }

    @Test
    @DisplayName("should return empty Mono when movement is not found by ID")
    void shouldReturnEmptyMonoWhenMovementNotFound() {
        // Arrange
        when(movementRepositoryPort.findById(any(Long.class))).thenReturn(Mono.empty());
        
        // Act & Assert
        StepVerifier.create(movementUseCaseImpl.findMovementById(99L))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("should find all movements for a specific account successfully")
    void shouldFindAllMovementsByAccountIdSuccessfully() {
        // Arrange
        when(movementRepositoryPort.findByAccountId(testAccount.getId())).thenReturn(Flux.just(testMovementCredit, testMovementDebit));
        
        // Act & Assert
        StepVerifier.create(movementUseCaseImpl.findMovementsByAccountId(testAccount.getId()))
                .expectNext(testMovementCredit, testMovementDebit)
                .verifyComplete();
    }



    @Test
    @DisplayName("should successfully delete a movement")
    void shouldDeleteMovementSuccessfully() {
        // Arrange
        when(movementRepositoryPort.findById(any(Long.class))).thenReturn(Mono.just(testMovementCredit));
        when(movementRepositoryPort.deleteById(any(Long.class))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(movementUseCaseImpl.deleteMovement(1L))
                .verifyComplete();

        verify(movementRepositoryPort, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("should fail to delete a movement if it does not exist")
    void shouldFailToDeleteMovementIfNotFound() {
        // Arrange
        when(movementRepositoryPort.findById(any(Long.class))).thenReturn(Mono.empty());
        
        // Act & Assert
        StepVerifier.create(movementUseCaseImpl.deleteMovement(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}