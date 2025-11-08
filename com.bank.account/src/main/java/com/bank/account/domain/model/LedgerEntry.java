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

    private Long id;

    private LocalDateTime timestamp;

    private Long movementId;

    private Long accountId;

    private LedgerEntryType entryType;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String description;

    private String initiatedBy;

    public enum LedgerEntryType {
        DEBIT,
        CREDIT
    }
}