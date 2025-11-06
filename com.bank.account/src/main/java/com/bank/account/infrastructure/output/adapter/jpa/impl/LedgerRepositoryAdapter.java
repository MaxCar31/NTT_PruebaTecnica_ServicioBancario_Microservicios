package com.bank.account.infrastructure.output.adapter.jpa.impl;

import com.bank.account.application.output.port.LedgerRepositoryPort;
import com.bank.account.domain.model.LedgerEntry;
import com.bank.account.infrastructure.output.adapter.jpa.mapper.LedgerJpaMapper;
import com.bank.account.infrastructure.output.adapter.jpa.repository.LedgerSpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA implementation of LedgerRepositoryPort.
 *
 * This adapter wraps blocking JPA calls in reactive Mono/Flux operators
 * and executes them on the boundedElastic scheduler to prevent blocking
 * the event loop in a reactive application.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerRepositoryAdapter implements LedgerRepositoryPort {

    private final LedgerSpringRepository ledgerSpringRepository;
    private final LedgerJpaMapper ledgerJpaMapper;

    @Override
    public Mono<LedgerEntry> saveLedgerEntry(LedgerEntry ledgerEntry) {
        log.debug("Saving ledger entry for account: {}, type: {}, amount: {}",
                ledgerEntry.getAccountId(), ledgerEntry.getEntryType(), ledgerEntry.getAmount());

        return Mono.fromCallable(() -> {
                    var entity = ledgerJpaMapper.toEntity(ledgerEntry);
                    var savedEntity = ledgerSpringRepository.save(entity);
                    return ledgerJpaMapper.toDomain(savedEntity);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(saved -> log.info("Ledger entry saved with ID: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to save ledger entry", error));
    }

    @Override
    public Flux<LedgerEntry> findByAccountId(Long accountId) {
        log.debug("Finding all ledger entries for account: {}", accountId);

        return Mono.fromCallable(() -> ledgerSpringRepository.findByAccountIdOrderByTimestampAsc(accountId))
                .flatMapMany(Flux::fromIterable)
                .map(ledgerJpaMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(() -> log.debug("Completed finding ledger entries for account: {}", accountId));
    }

    @Override
    public Flux<LedgerEntry> findByAccountIdAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding ledger entries for account: {} between {} and {}", accountId, startDate, endDate);

        return Mono.fromCallable(() ->
                        ledgerSpringRepository.findByAccountIdAndTimestampBetween(accountId, startDate, endDate))
                .flatMapMany(Flux::fromIterable)
                .map(ledgerJpaMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<LedgerEntry> findByAccountIdsAndDateRange(List<Long> accountIds, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding ledger entries for {} accounts between {} and {}", accountIds.size(), startDate, endDate);

        return Mono.fromCallable(() ->
                        ledgerSpringRepository.findByAccountIdsAndTimestampBetween(accountIds, startDate, endDate))
                .flatMapMany(Flux::fromIterable)
                .map(ledgerJpaMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<LedgerEntry> findByMovementId(Long movementId) {
        log.debug("Finding ledger entries for movement: {}", movementId);

        return Mono.fromCallable(() -> ledgerSpringRepository.findByMovementId(movementId))
                .flatMapMany(Flux::fromIterable)
                .map(ledgerJpaMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> countByAccountId(Long accountId) {
        log.debug("Counting ledger entries for account: {}", accountId);

        return Mono.fromCallable(() -> ledgerSpringRepository.countByAccountId(accountId))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(count -> log.debug("Account {} has {} ledger entries", accountId, count));
    }
}