package com.bank.account.application.input.port;

import com.bank.account.domain.model.AccountStatement;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

public interface ReportUseCase {
    Mono<AccountStatement> generateAccountStatement(Long clientId, String accountNumber, LocalDateTime startDate, LocalDateTime endDate);
}