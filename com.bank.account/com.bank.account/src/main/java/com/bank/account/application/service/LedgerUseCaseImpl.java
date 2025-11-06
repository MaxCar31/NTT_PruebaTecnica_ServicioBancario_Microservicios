package com.bank.account.application.service;

import com.bank.account.application.input.port.LedgerUseCase;
import com.bank.account.application.output.port.LedgerRepositoryPort;
import com.bank.account.domain.model.LedgerEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of LedgerUseCase.
 * This service is responsible for managing the ledger (audit trail) of all
 * financial operations in the system. It follows the Event Sourcing pattern
 * where all changes are recorded as immutable events.
 * Key principles:
 * - All ledger entries are append-only (no updates or deletes)
 * - Each entry records the state before and after a transaction
 * - Complete audit trail for compliance and fraud detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerUseCaseImpl implements LedgerUseCase {

    private final LedgerRepositoryPort ledgerRepositoryPort;

    @Override
    public Mono<LedgerEntry> recordLedgerEntry(LedgerEntry ledgerEntry) {
        log.info("Recording ledger entry for account: {}, type: {}, amount: {}",
                ledgerEntry.getAccountId(), ledgerEntry.getEntryType(), ledgerEntry.getAmount());

    
        return validateLedgerEntry(ledgerEntry)
                .then(ledgerRepositoryPort.saveLedgerEntry(ledgerEntry))
                .doOnSuccess(saved ->
                        log.info("Successfully recorded ledger entry with ID: {} for account: {}",
                                saved.getId(), saved.getAccountId()))
                .doOnError(error ->
                        log.error("Failed to record ledger entry for account: {}",
                                ledgerEntry.getAccountId(), error));
    }

    @Override
    public Flux<LedgerEntry> getAccountLedger(Long accountId) {
        log.info("Retrieving complete ledger for account: {}", accountId);

        return ledgerRepositoryPort.findByAccountId(accountId)
                .doOnComplete(() -> log.info("Successfully retrieved ledger for account: {}", accountId));
    }

    @Override
    public Flux<LedgerEntry> getAccountLedgerByDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Retrieving ledger for account: {} between {} and {}", accountId, startDate, endDate);

   
        if (startDate.isAfter(endDate)) {
            log.error("Invalid date range: start date {} is after end date {}", startDate, endDate);
            return Flux.error(new IllegalArgumentException("Start date must be before end date"));
        }

        return ledgerRepositoryPort.findByAccountIdAndDateRange(accountId, startDate, endDate)
                .doOnComplete(() ->
                        log.info("Successfully retrieved ledger entries for account: {} in date range", accountId));
    }

    @Override
    public Flux<LedgerEntry> getLedgerByAccountsAndDateRange(List<Long> accountIds, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Retrieving ledger for {} accounts between {} and {}", accountIds.size(), startDate, endDate);

     
        if (accountIds == null || accountIds.isEmpty()) {
            log.error("Account IDs list is empty or null");
            return Flux.error(new IllegalArgumentException("Account IDs cannot be empty"));
        }

        if (startDate.isAfter(endDate)) {
            log.error("Invalid date range: start date {} is after end date {}", startDate, endDate);
            return Flux.error(new IllegalArgumentException("Start date must be before end date"));
        }

        return ledgerRepositoryPort.findByAccountIdsAndDateRange(accountIds, startDate, endDate)
                .doOnComplete(() ->
                        log.info("Successfully retrieved ledger entries for {} accounts", accountIds.size()));
    }

    @Override
    public Flux<LedgerEntry> getLedgerByMovement(Long movementId) {
        log.info("Retrieving ledger entries for movement: {}", movementId);

        return ledgerRepositoryPort.findByMovementId(movementId)
                .doOnComplete(() ->
                        log.info("Successfully retrieved ledger entries for movement: {}", movementId));
    }

    @Override
    public Mono<Long> getAccountLedgerEntryCount(Long accountId) {
        log.info("Counting ledger entries for account: {}", accountId);

        return ledgerRepositoryPort.countByAccountId(accountId)
                .doOnSuccess(count ->
                        log.info("Account {} has {} ledger entries", accountId, count));
    }

    /**
     * Validates that a ledger entry has all required fields.
     *
     * @param ledgerEntry The entry to validate
     * @return A Mono that completes if valid, or errors if invalid
     */
    private Mono<Void> validateLedgerEntry(LedgerEntry ledgerEntry) {
        if (ledgerEntry.getAccountId() == null) {
            return Mono.error(new IllegalArgumentException("Account ID is required"));
        }
        if (ledgerEntry.getMovementId() == null) {
            return Mono.error(new IllegalArgumentException("Movement ID is required"));
        }
        if (ledgerEntry.getEntryType() == null) {
            return Mono.error(new IllegalArgumentException("Entry type is required"));
        }
        if (ledgerEntry.getAmount() == null || ledgerEntry.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Amount must be positive"));
        }
        if (ledgerEntry.getBalanceBefore() == null || ledgerEntry.getBalanceAfter() == null) {
            return Mono.error(new IllegalArgumentException("Balance before and after are required"));
        }

        return Mono.empty();
    }
}