package com.bank.account.infrastructure.output.adapter.jpa.mapper;

import com.bank.account.domain.model.Movement;
import com.bank.account.infrastructure.output.adapter.jpa.entity.MovementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovementJpaMapper {

    @Mapping(source = "accountId", target = "account.id")
    MovementEntity toEntity(Movement movement);

    @Mapping(source = "account.id", target = "accountId")
    Movement toDomain(MovementEntity movementEntity);
}