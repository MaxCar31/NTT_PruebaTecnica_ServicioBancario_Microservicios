package com.bank.account.application.input.port;

import com.bank.account.domain.model.Movement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementUseCase {
    Mono<Movement> registerMovement(Movement movement);
    Flux<Movement> findAllMovements();
    Flux<Movement> findMovementsByAccountId(Long accountId);
    Mono<Movement> findMovementById(Long id);
    Mono<Void> deleteMovement(Long id);
}