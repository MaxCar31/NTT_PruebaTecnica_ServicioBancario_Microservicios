package com.bank.account.application.service;

import com.bank.account.application.input.port.LedgerUseCase;
import com.bank.account.application.input.port.MovementUseCase;
import com.bank.account.application.output.port.AccountRepositoryPort;
import com.bank.account.application.output.port.MovementRepositoryPort;
import com.bank.account.domain.exception.InsufficientBalanceException;
import com.bank.account.domain.exception.ResourceNotFoundException;
import com.bank.account.domain.model.Account;
import com.bank.account.domain.model.LedgerEntry;
import com.bank.account.domain.model.Movement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementUseCaseImpl implements MovementUseCase {

    private static final String DEBIT = "Debit";
    private static final String CREDIT = "Credit";

    private final MovementRepositoryPort movementRepositoryPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final LedgerUseCase ledgerUseCase;
    private final PlatformTransactionManager transactionManager;

    @Override
    public Mono<Movement> registerMovement(Movement movement) {
        log.info("Attempting to register a movement of amount {} for account id: {}",
                movement.getAmount(), movement.getAccountId());

        // Validar monto en la cadena reactiva ANTES de entrar a transacción
        return Mono.fromRunnable(() -> validateMovementAmount(movement))
                .then(Mono.fromCallable(() -> {
                    // Usamos TransactionTemplate para operaciones JPA bloqueantes
                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                    return transactionTemplate.execute(status -> executeMovementTransaction(movement));
                }))
                .subscribeOn(Schedulers.boundedElastic()); // Ejecutar en un hilo separado
    }

    /**
     * Ejecuta la transacción atómica de movimiento.
     * AHORA INCLUYE EL REGISTRO EN EL LEDGER.
     */
    private Movement executeMovementTransaction(Movement movement) {
        Account account = fetchAndValidateAccount(movement.getAccountId());
        BigDecimal previousBalance = account.getInitialBalance();
        BigDecimal newBalance = calculateNewBalance(account, movement);

        updateMovementDetails(movement, newBalance);
        updateAccountBalance(account, newBalance);

        // Persistir movimiento y cuenta
        Movement savedMovement = persistMovement(movement, account, previousBalance, newBalance);

        // NEW: Registrar en el Ledger (Event Sourcing)
        recordLedgerEntry(savedMovement, account, previousBalance, newBalance);

        return savedMovement;
    }


    private void recordLedgerEntry(Movement movement, Account account, BigDecimal balanceBefore, BigDecimal balanceAfter) {

        LedgerEntry.LedgerEntryType entryType = movement.getMovementType().equals(DEBIT)
                ? LedgerEntry.LedgerEntryType.DEBIT
                : LedgerEntry.LedgerEntryType.CREDIT;


        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .timestamp(movement.getDate())
                .movementId(movement.getId())
                .accountId(account.getId())
                .entryType(entryType)
                .amount(movement.getAmount().abs())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(String.format("%s of %s on account %s",
                        movement.getMovementType(),
                        movement.getAmount(),
                        account.getAccountNumber()))
                .initiatedBy("SYSTEM")
                .build();


        ledgerUseCase.recordLedgerEntry(ledgerEntry)
                .block();

        log.info("Ledger entry recorded for movement ID: {} on account: {}",
                movement.getId(), account.getId());
    }

    /**
     * Obtiene la cuenta y valida que exista.
     * Lanza ResourceNotFoundException si no existe.
     */
    private Account fetchAndValidateAccount(Long accountId) {
        return accountRepositoryPort.findAccountById(accountId)
                .blockOptional()
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    /**
     * Valida que el monto del movimiento no sea cero.
     * Lanza IllegalArgumentException si es inválido.
     */
    private void validateMovementAmount(Movement movement) {
        if (movement.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            log.error("Failed to register movement: The movement amount cannot be zero for account id: {}",
                    movement.getAccountId());
            throw new IllegalArgumentException("The movement amount cannot be zero.");
        }
    }

    /**
     * Calcula el nuevo balance basado en el tipo de movimiento (débito o crédito).
     * Lanza InsufficientBalanceException si no hay saldo suficiente para débitos.
     */
    private BigDecimal calculateNewBalance(Account account, Movement movement) {
        BigDecimal previousBalance = account.getInitialBalance();
        BigDecimal newBalance;

        if (isDebit(movement)) {
            movement.setMovementType(DEBIT);
            BigDecimal resultingBalance = previousBalance.add(movement.getAmount());

            if (resultingBalance.compareTo(BigDecimal.ZERO) < 0) {
                log.error("Insufficient balance for debit movement. Current balance: {}, Requested amount: {}",
                        previousBalance, movement.getAmount());
                throw new InsufficientBalanceException("Insufficient balance");
            }

            newBalance = resultingBalance;
            log.info("Debit movement processed. Previous balance: {}, New balance: {}", previousBalance, newBalance);
        } else {
            movement.setMovementType(CREDIT);
            newBalance = previousBalance.add(movement.getAmount());
            log.info("Credit movement processed. Previous balance: {}, New balance: {}", previousBalance, newBalance);
        }

        return newBalance;
    }

    private boolean isDebit(Movement movement) {
        return movement.getAmount().compareTo(BigDecimal.ZERO) < 0;
    }

    private void updateMovementDetails(Movement movement, BigDecimal newBalance) {
        movement.setDate(LocalDateTime.now());
        movement.setBalance(newBalance);
    }

    private void updateAccountBalance(Account account, BigDecimal newBalance) {
        account.setInitialBalance(newBalance);
    }

    private Movement persistMovement(Movement movement, Account account,
                                     BigDecimal previousBalance, BigDecimal newBalance) {
        accountRepositoryPort.saveAccount(account).block();
        Movement savedMovement = movementRepositoryPort.saveMovement(movement)
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Movement could not be persisted"));

        log.info("Movement successfully registered with id: {}. Previous balance: {}, New balance: {}",
                savedMovement.getId(), previousBalance, newBalance);
        return savedMovement;
    }

    @Override
    public Flux<Movement> findAllMovements() {
        log.info("Retrieving all movements in the system.");
        return movementRepositoryPort.findAll()
                .doOnComplete(() -> log.info("All movements retrieved successfully."));
    }

    @Override
    public Flux<Movement> findMovementsByAccountId(Long accountId) {
        log.info("Retrieving all movements for account with ID: {}", accountId);
        return movementRepositoryPort.findByAccountId(accountId)
                .doOnComplete(() -> log.info("All movements retrieved successfully for account with ID: {}", accountId));
    }

    @Override
    public Mono<Movement> findMovementById(Long id) {
        log.info("Searching for movement with ID: {}", id);
        return movementRepositoryPort.findById(id)
                .doOnSuccess(movement -> {
                    if (movement != null) {
                        log.info("Movement found with ID: {}", id);
                    } else {
                        log.warn("Movement not found for ID: {}", id);
                    }
                });
    }

    @Override
    public Mono<Void> deleteMovement(Long id) {
        log.warn("Attempting to delete a movement with ID: {}. This is not recommended for financial records.", id);
        return movementRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(() -> {
                    log.error("Failed to delete movement: Movement not found with ID: {}", id);
                    return new ResourceNotFoundException("Movement not found with ID: " + id);
                }))
                .flatMap(movement -> movementRepositoryPort.deleteById(id)
                        .doOnSuccess(aVoid -> log.info("Successfully deleted movement with ID: {}", id)));
    }
}