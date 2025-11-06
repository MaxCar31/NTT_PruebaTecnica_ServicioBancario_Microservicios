package com.bank.account.infrastructure.output.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for ledger entries in the database.
 * This table is append-only (immutable) for audit purposes.
 *
 * Indexes are optimized for common queries:
 * - By account ID and timestamp (to get account history)
 * - By movement ID (to trace the source of an entry)
 */
@Getter
@Setter
@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_account_timestamp", columnList = "account_id, timestamp"),
        @Index(name = "idx_ledger_movement", columnList = "movement_id")
})
public class LedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "movement_id", nullable = false)
    private Long movementId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private LedgerEntryType entryType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    @Column(name = "initiated_by", length = 100)
    private String initiatedBy;

    /**
     * Automatically set timestamp before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public enum LedgerEntryType {
        DEBIT,
        CREDIT
    }
}