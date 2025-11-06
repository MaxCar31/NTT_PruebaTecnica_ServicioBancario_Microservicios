package com.bank.account.infrastructure.input.adapter.rest.mapper;

import com.bank.account.domain.model.LedgerEntry;
import com.bank.account.infrastructure.input.adapter.rest.dto.response.LedgerEntryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LedgerRestMapper {

    // Mapea el Enum a String automáticamente
    @Mapping(source = "entryType", target = "entryType") 
    LedgerEntryResponse toResponse(LedgerEntry domain);

    // Opcional, por si necesitas el camino inverso
    @Mapping(source = "entryType", target = "entryType")
    LedgerEntry toDomain(LedgerEntryResponse dto);
}