package com.bank.account.infrastructure.output.adapter.jpa.impl;

import com.bank.account.application.output.port.MovementRepositoryPort;
import com.bank.account.domain.model.Movement;
import com.bank.account.infrastructure.output.adapter.jpa.mapper.MovementJpaMapper;
import com.bank.account.infrastructure.output.adapter.jpa.repository.MovementSpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovementRepositoryAdapter implements MovementRepositoryPort {

    private final MovementSpringRepository movementRepository;
    private final MovementJpaMapper movementMapper;

    @Override
    public Mono<Movement> saveMovement(Movement movement) {
        return Mono.fromCallable(() -> {
            var movementEntity = movementMapper.toEntity(movement);
            return movementMapper.toDomain(movementRepository.save(movementEntity));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Movement> findById(Long id) {
        return Mono.fromCallable(() -> movementRepository.findById(id)
                        .map(movementMapper::toDomain)
                        .orElse(null))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Movement> findByAccountId(Long accountId) {
        return Mono.fromCallable(() -> movementRepository.findByAccountId(accountId))
                .flatMapMany(Flux::fromIterable)
                .map(movementMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Movement> findAll() {
        return Mono.fromCallable(() -> movementRepository.findAll())
                .flatMapMany(Flux::fromIterable)
                .map(movementMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> movementRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Flux<Movement> findMovementsByAccountIdsAndDateRange(List<Long> accountIds, LocalDateTime startDate, LocalDateTime endDate) {
        return Mono.fromCallable(() -> movementRepository.findByAccountIdInAndDateBetween(
                        accountIds,
                        startDate,
                        endDate
                ))
                .flatMapMany(Flux::fromIterable)
                .map(movementMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }
}