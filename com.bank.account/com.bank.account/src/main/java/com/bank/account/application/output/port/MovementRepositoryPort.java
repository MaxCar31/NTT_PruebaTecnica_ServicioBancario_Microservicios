package com.bank.account.application.output.port;

import com.bank.account.domain.model.Movement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.List;

public interface MovementRepositoryPort {
    Mono<Movement> saveMovement(Movement movement);
    Mono<Movement> findById(Long id);
    Flux<Movement> findByAccountId(Long accountId);
    Flux<Movement> findAll();
    Mono<Void> deleteById(Long id);
    Flux<Movement> findMovementsByAccountIdsAndDateRange(List<Long> accountIds, LocalDateTime startDate, LocalDateTime endDate);
}