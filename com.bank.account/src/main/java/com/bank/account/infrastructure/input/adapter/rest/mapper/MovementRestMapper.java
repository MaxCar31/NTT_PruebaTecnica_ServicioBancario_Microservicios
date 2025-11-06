package com.bank.account.infrastructure.input.adapter.rest.mapper;

import com.bank.account.domain.model.Movement;
import com.bank.account.infrastructure.input.adapter.rest.dto.request.MovementRequest;
import com.bank.account.infrastructure.input.adapter.rest.dto.response.MovementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovementRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "movementType", ignore = true)
    @Mapping(target = "balance", ignore = true)
    Movement toDomain(MovementRequest movementRequest);

    MovementResponse toResponse(Movement movement);
}