package com.bank.account.infrastructure.input.adapter.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class AccountRequest {

    @Pattern(regexp = "^[0-9]{10,20}$", message = "Account number must be 10-20 digits")
    private String accountNumber;

    @Pattern(regexp = "^(SAVINGS|CHECKING|CREDIT)$", message = "Account type must be SAVINGS, CHECKING or CREDIT")
    private String accountType;

    @NotNull(message = "Initial balance cannot be null")
    private BigDecimal initialBalance;

    @NotNull(message = "Status cannot be null")
    private Boolean status;

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;
}