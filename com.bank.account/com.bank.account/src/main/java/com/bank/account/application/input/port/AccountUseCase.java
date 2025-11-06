package com.bank.account.application.input.port;
import com.bank.account.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountUseCase {
    Mono<Account> createAccount(Account account);
    Mono<Account> findAccountById(Long id);
    Mono<Account> findAccountByNumber(String accountNumber);
    Flux<Account> findAllAccounts();
    Mono<Account> updateAccount(Long id, Account account);
    Mono<Void> deleteAccount(Long id);
}