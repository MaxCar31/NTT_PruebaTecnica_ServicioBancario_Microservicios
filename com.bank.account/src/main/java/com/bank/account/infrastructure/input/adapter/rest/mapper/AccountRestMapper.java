package com.bank.account.infrastructure.input.adapter.rest.mapper;

import com.bank.account.domain.model.Account;
import com.bank.account.infrastructure.input.adapter.rest.dto.request.AccountRequest;
import com.bank.account.infrastructure.input.adapter.rest.dto.response.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountRestMapper {

    @Mapping(target = "id", ignore = true)
    Account toDomain(AccountRequest accountRequest);

    AccountResponse toResponse(Account account);
}