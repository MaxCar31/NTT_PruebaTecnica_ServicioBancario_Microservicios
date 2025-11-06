package com.bank.account.infrastructure.output.adapter.jpa.mapper;

import com.bank.account.domain.model.LedgerEntry;
import com.bank.account.infrastructure.output.adapter.jpa.entity.LedgerEntryEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper between LedgerEntry domain model and LedgerEntryEntity JPA entity.
 * Note: The enum mapping is automatic because both enums have the same names (DEBIT, CREDIT).
 * MapStruct will handle the conversion automatically.
 */
@Mapper(componentModel = "spring")
public interface LedgerJpaMapper {

    /**
     * Converts domain model to JPA entity.
     * Used when persisting a new ledger entry.
     */
    LedgerEntryEntity toEntity(LedgerEntry ledgerEntry);

    /**
     * Converts JPA entity to domain model.
     * Used when retrieving ledger entries from the database.
     */
    LedgerEntry toDomain(LedgerEntryEntity ledgerEntryEntity);
}