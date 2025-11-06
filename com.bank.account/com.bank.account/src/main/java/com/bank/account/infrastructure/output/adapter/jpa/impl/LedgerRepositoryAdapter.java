package com.bank.account.infrastructure.output.adapter.jpa.impl;

import com.bank.account.application.output.port.AccountRepositoryPort;
import com.bank.account.domain.model.Account;
import com.bank.account.infrastructure.output.adapter.jpa.mapper.AccountJpaMapper;
import com.bank.account.infrastructure.output.adapter.jpa.repository.AccountSpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountSpringRepository accountRepository;
    private final AccountJpaMapper accountMapper;

    @Override
    public Mono<Account> saveAccount(Account account) {
        return Mono.fromCallable(() -> {
            var accountEntity = accountMapper.toEntity(account);
            return accountMapper.toDomain(accountRepository.save(accountEntity));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Account> findAccountByNumber(String accountNumber) {
        return Mono.fromCallable(() -> accountRepository.findByAccountNumber(accountNumber)
                        .map(accountMapper::toDomain))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Account> findAccountById(Long id) {
        return Mono.fromCallable(() -> accountRepository.findById(id)
                        .map(accountMapper::toDomain))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteAccountById(Long id) {
        return Mono.fromRunnable(() -> accountRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Flux<Account> findAll() {
        return Flux.fromIterable(accountRepository.findAllByStatusIsTrue())
                .map(accountMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Account> findByCustomerId(Long customerId) {
        // Lógica actualizada para buscar por el campo customerId
        return Mono.fromCallable(() -> accountRepository.findByCustomerId(customerId))
                .flatMapMany(Flux::fromIterable)
                .map(accountMapper::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Account> findAccountByAccountNumberAndIdNot(String accountNumber, Long accountId) {
        return Mono.fromCallable(() -> accountRepository.findByAccountNumberAndIdNot(accountNumber, accountId)
                        .map(accountMapper::toDomain))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }
}