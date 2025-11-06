package com.bank.account.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AccountStatement {
    private String clientName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<AccountReportDetail> accounts;

    @Data
    @Builder
    public static class AccountReportDetail {
        private String accountNumber;
        private String accountType;
        private BigDecimal initialBalance;
        private BigDecimal finalBalance;
        private List<MovementReportDetail> movements;
    }

    @Data
    @Builder
    public static class MovementReportDetail {
        private LocalDateTime date;
        private String movementType;
        private BigDecimal amount;
        private BigDecimal balanceAfterMovement;
    }
}