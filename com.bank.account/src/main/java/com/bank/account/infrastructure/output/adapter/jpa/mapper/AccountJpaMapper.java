package com.bank.account.infrastructure.output.adapter.jpa.mapper;

import com.bank.account.domain.model.Account;
import com.bank.account.infrastructure.output.adapter.jpa.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountJpaMapper {

    @Mapping(source = "customerId", target = "customerId")
    AccountEntity toEntity(Account account);

    @Mapping(source = "customerId", target = "customerId")
    Account toDomain(AccountEntity accountEntity);
}