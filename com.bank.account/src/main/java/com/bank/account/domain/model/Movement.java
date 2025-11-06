package com.bank.account.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class Movement {
    private Long id;
    private LocalDateTime date;
    private String movementType;
    private BigDecimal amount;
    private BigDecimal balance;
    private Long accountId;
}