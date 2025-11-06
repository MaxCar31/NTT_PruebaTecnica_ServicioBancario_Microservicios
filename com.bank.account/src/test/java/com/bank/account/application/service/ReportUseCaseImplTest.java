package com.bank.account.application.service;

import com.bank.account.application.output.port.AccountRepositoryPort;
import com.bank.account.application.output.port.CustomerClientPort;
import com.bank.account.application.output.port.LedgerRepositoryPort;
import com.bank.account.domain.exception.ResourceNotFoundException;
import com.bank.account.domain.model.Account;
import com.bank.account.domain.model.AccountStatement;
import com.bank.account.domain.model.Customer;
import com.bank.account.domain.model.LedgerEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Report Use Case Unit Tests")
class ReportUseCaseImplTest {

    @Mock
    private CustomerClientPort customerClientPort;
    @Mock
    private AccountRepositoryPort accountRepositoryPort;
    @Mock
    private LedgerRepositoryPort ledgerRepositoryPort;

    @InjectMocks
    private ReportUseCaseImpl reportUseCase;

    private final LocalDateTime startDate = LocalDateTime.of(2025, 10, 1, 0, 0);
    private final LocalDateTime endDate = LocalDateTime.of(2025, 10, 31, 23, 59);

    /**
     * Prueba el "camino feliz": un cliente con una cuenta y movimientos.
     */
    @Test
    @DisplayName("should generate statement successfully when client exists with movements")
    void generateAccountStatement_Success_WhenClientExistsWithMovements() {
        // Arrange
        Long clientId = 1L;

        // Cliente (DTO)
        Customer mockCustomer = Customer.builder()
                .customerId(clientId)
                .name("Jose Lema")
                .build();

        // Cuenta (Entidad de este servicio)
        Account mockAccount = Account.builder()
                .id(101L)
                .accountNumber("478758")
                .accountType("Ahorro")
                // El saldo de la cuenta (950) es el estado *final*
                .initialBalance(new BigDecimal("950.00"))
                .customerId(clientId)
                .build();

        // Este objeto simula la entrada en el libro contable
        LedgerEntry mockLedgerEntry = LedgerEntry.builder()
                .accountId(101L)
                .timestamp(startDate.plusDays(5))
                .entryType(LedgerEntry.LedgerEntryType.DEBIT)
                .amount(new BigDecimal("50.00"))
                // El reporte usa 'balanceBefore' como el saldo inicial del periodo [cite: 468]
                .balanceBefore(new BigDecimal("1000.00"))
                // El reporte usa 'balanceAfter' como el saldo final del periodo [cite: 461]
                .balanceAfter(new BigDecimal("950.00"))
                .build();

        // Simulación de las llamadas
        when(customerClientPort.findCustomerById(clientId)).thenReturn(Mono.just(mockCustomer));
        when(accountRepositoryPort.findByCustomerId(clientId)).thenReturn(Flux.just(mockAccount));


        when(ledgerRepositoryPort.findByAccountIdsAndDateRange(any(List.class), any(), any()))
                .thenReturn(Flux.just(mockLedgerEntry));

        // Act
        Mono<AccountStatement> result = reportUseCase.generateAccountStatement(clientId, null, startDate, endDate);

        // Assert
        StepVerifier.create(result)
                .assertNext(statement -> {
                    assertNotNull(statement);
                    assertEquals("Jose Lema", statement.getClientName());
                    assertEquals(1, statement.getAccounts().size());

                    AccountStatement.AccountReportDetail accountDetail = statement.getAccounts().get(0);
                    assertEquals("478758", accountDetail.getAccountNumber());
                    assertEquals(1, accountDetail.getMovements().size());

                    // Saldo ANTES del primer movimiento (tomado de mockLedgerEntry.balanceBefore)
                    assertEquals(new BigDecimal("1000.00"), accountDetail.getInitialBalance());
                    // Saldo DESPUÉS del último movimiento (tomado de mockLedgerEntry.balanceAfter)
                    assertEquals(new BigDecimal("950.00"), accountDetail.getFinalBalance());
                })
                .verifyComplete();
    }

    /**
     * Prueba el caso en que un cliente existe pero no tiene movimientos en el rango de fechas.
     */
    @Test
    @DisplayName("should generate statement successfully when client has no movements")
    void generateAccountStatement_Success_WhenClientHasNoMovementsInDateRange() {
        // Arrange
        Long clientId = 2L;
        Customer mockCustomer = Customer.builder().customerId(clientId).name("Marianela Montalvo").build();
        Account mockAccount = Account.builder().id(201L).accountNumber("225487").accountType("Corriente").initialBalance(new BigDecimal("500")).customerId(clientId).build();

        when(customerClientPort.findCustomerById(clientId)).thenReturn(Mono.just(mockCustomer));
        when(accountRepositoryPort.findByCustomerId(clientId)).thenReturn(Flux.just(mockAccount));
        when(ledgerRepositoryPort.findByAccountIdsAndDateRange(any(List.class), any(), any())).thenReturn(Flux.empty()); // No hay movimientos

        // Act
        Mono<AccountStatement> result = reportUseCase.generateAccountStatement(clientId, null, startDate, endDate);

        // Assert
        StepVerifier.create(result)
                .assertNext(statement -> {
                    assertNotNull(statement);
                    assertEquals("Marianela Montalvo", statement.getClientName());
                    assertEquals(1, statement.getAccounts().size());

                    AccountStatement.AccountReportDetail accountDetail = statement.getAccounts().get(0);
                    assertEquals("225487", accountDetail.getAccountNumber());
                    assertTrue(accountDetail.getMovements().isEmpty()); // Lista de movimientos vacía

                    // Si no hay movimientos, el saldo inicial y final es el saldo actual de la cuenta
                    assertEquals(new BigDecimal("500"), accountDetail.getInitialBalance());
                    assertEquals(new BigDecimal("500"), accountDetail.getFinalBalance());
                })
                .verifyComplete();
    }

    /**
     * Prueba el caso en que el cliente solicitado no existe en la base de datos.
     */
    @Test
    @DisplayName("should fail when client is not found")
    void generateAccountStatement_Fails_WhenClientNotFound() {
        // Arrange
        Long nonExistentClientId = 999L;

        when(customerClientPort.findCustomerById(nonExistentClientId)).thenReturn(Mono.empty());
        when(accountRepositoryPort.findByCustomerId(nonExistentClientId)).thenReturn(Flux.empty());

        // Act
        Mono<AccountStatement> result = reportUseCase.generateAccountStatement(nonExistentClientId, null, startDate, endDate);

        // Assert
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}