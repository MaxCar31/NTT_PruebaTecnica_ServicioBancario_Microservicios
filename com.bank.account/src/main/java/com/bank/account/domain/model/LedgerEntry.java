package com.bank.account.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an immutable ledger entry in the double-entry bookkeeping system.
 * This follows the Event Sourcing pattern where every financial operation
 * is recorded as an append-only log entry.
 *
 * In accounting, every transaction affects at least two accounts:
 * - A debit entry in one account
 * - A credit entry in another account
 *
 * For our banking system:
 * - When a customer deposits $100, we create a CREDIT entry (money IN)
 * - When a customer withdraws $100, we create a DEBIT entry (money OUT)
 *
 * Use cases:
 * - Complete audit trail of all account operations
 * - Reconstruct account state at any point in time
 * - Compliance and regulatory reporting
 * - Detect fraud or suspicious activities
 */
@Data
@Builder(toBuilder = true)
public class LedgerEntry {

    /** Unique identifier for this ledger entry */
    private Long id;

    /** When this entry was created (immutable timestamp) */
    private LocalDateTime timestamp;

    /** Reference to the movement that generated this entry */
    private Long movementId;

    /** Reference to the affected account */
    private Long accountId;

    /** Type of ledger entry: DEBIT (money out) or CREDIT (money in) */
    private LedgerEntryType entryType;

    /** Amount of this entry (always positive, sign is determined by entryType) */
    private BigDecimal amount;

    /** Account balance BEFORE this entry was applied */
    private BigDecimal balanceBefore;

    /** Account balance AFTER this entry was applied */
    private BigDecimal balanceAfter;

    /** Optional description or notes about this entry */
    private String description;

    /** User or system that initiated this entry (for audit purposes) */
    private String initiatedBy;

    /**
     * Type of ledger entry in double-entry bookkeeping
     */
    public enum LedgerEntryType {
        /** Debit: Money leaving the account (withdrawal, payment, transfer out) */
        DEBIT,

        /** Credit: Money entering the account (deposit, transfer in) */
        CREDIT
    }
}