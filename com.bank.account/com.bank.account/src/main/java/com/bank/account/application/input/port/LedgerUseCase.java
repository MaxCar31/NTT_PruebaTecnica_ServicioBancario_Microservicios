package com.bank.account.application.input.port;

import com.bank.account.domain.model.LedgerEntry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Input port defining use cases for ledger entry operations.
 * This interface defines the business operations available for the ledger system.
 */
public interface LedgerUseCase {

    /**
     * Records a new ledger entry.
     * This is typically called internally when a movement is created.
     *
     * @param ledgerEntry The ledger entry to record
     * @return A Mono that emits the saved ledger entry
     */
    Mono<LedgerEntry> recordLedgerEntry(LedgerEntry ledgerEntry);

    /**
     * Retrieves the complete ledger (audit trail) for an account.
     *
     * @param accountId The account ID
     * @return A Flux of all ledger entries for the account
     */
    Flux<LedgerEntry> getAccountLedger(Long accountId);

    /**
     * Retrieves ledger entries for an account within a date range.
     * Used for generating periodic account statements.
     *
     * @param accountId The account ID
     * @param startDate Start of the period
     * @param endDate End of the period
     * @return A Flux of ledger entries in the period
     */
    Flux<LedgerEntry> getAccountLedgerByDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves ledger entries for multiple accounts within a date range.
     * Used for consolidated reporting.
     *
     * @param accountIds List of account IDs
     * @param startDate Start of the period
     * @param endDate End of the period
     * @return A Flux of ledger entries for all accounts
     */
    Flux<LedgerEntry> getLedgerByAccountsAndDateRange(List<Long> accountIds, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves all ledger entries associated with a specific movement.
     *
     * @param movementId The movement ID
     * @return A Flux of ledger entries for the movement
     */
    Flux<LedgerEntry> getLedgerByMovement(Long movementId);

    /**
     * Gets statistics about an account's ledger.
     *
     * @param accountId The account ID
     * @return A Mono with the total count of ledger entries
     */
    Mono<Long> getAccountLedgerEntryCount(Long accountId);
}