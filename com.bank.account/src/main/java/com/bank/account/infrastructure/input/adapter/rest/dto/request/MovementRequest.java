package com.bank.account.infrastructure.input.adapter.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class MovementRequest {

    @NotNull(message = "Account ID cannot be null")
    private Long accountId;

    @NotNull(message = "Movement amount cannot be null")
    private BigDecimal amount;
}