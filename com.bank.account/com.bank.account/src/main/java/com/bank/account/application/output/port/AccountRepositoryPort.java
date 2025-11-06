package com.bank.account.application.output.port;

import com.bank.account.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepositoryPort {
    Mono<Account> saveAccount(Account account);
    Mono<Account> findAccountByNumber(String accountNumber);
    Mono<Account> findAccountById(Long id);
    Mono<Void> deleteAccountById(Long id);
    Flux<Account> findAll();
    Flux<Account> findByCustomerId(Long customerId);
    Mono<Account> findAccountByAccountNumberAndIdNot(String accountNumber, Long accountId);
}