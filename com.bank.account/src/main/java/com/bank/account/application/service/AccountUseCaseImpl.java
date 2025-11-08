package com.bank.account.application.service;

import com.bank.account.application.input.port.AccountUseCase;
import com.bank.account.application.output.port.CustomerClientPort;
import com.bank.account.application.output.port.AccountRepositoryPort;
import com.bank.account.domain.exception.DuplicateResourceException;
import com.bank.account.domain.exception.ResourceNotFoundException;
import com.bank.account.domain.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountUseCaseImpl implements AccountUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final CustomerClientPort customerClientPort;

    @Override
    public Mono<Account> createAccount(Account account) {
        log.info("Attempting to create a new account with number: {}", account.getAccountNumber());
        return validateUniqueAccountNumber(account.getAccountNumber())
                .then(validateCustomerExists(account.getCustomerId()))

                .then(Mono.defer(() -> {
                    log.info("All validations successful. Saving account.");
                    return accountRepositoryPort.saveAccount(account)
                            .doOnSuccess(acc -> log.info("Successfully created account with id: {}", acc.getId()));
                }));
    }

    @Override
    public Mono<Account> findAccountByNumber(String accountNumber) {
        log.info("Searching for account with number: {}", accountNumber);
        return accountRepositoryPort.findAccountByNumber(accountNumber)
                .doOnSuccess(account -> {
                    if (account != null) {
                        log.info("Account found with number: {}", accountNumber);
                    } else {
                        log.warn("Account not found for number: {}", accountNumber);
                    }
                });
    }

    private Mono<Void> validateUniqueAccountNumber(String accountNumber) {
        return accountRepositoryPort.findAccountByNumber(accountNumber)
                .flatMap(existingAccount -> {
                    String errorMsg = "Account number '" + accountNumber + "' already exists.";
                    log.warn(errorMsg);
                    return Mono.error(new DuplicateResourceException(errorMsg));
                }).then();
    }

    private Mono<Void> validateCustomerExists(Long customerId) {
        log.info("Validating existence of customer with ID: {}", customerId);
        return customerClientPort.findCustomerById(customerId)
                .switchIfEmpty(Mono.defer(() -> {
                    String errorMsg = "Customer not found for id: " + customerId;
                    log.error("Failed to create account: {}", errorMsg);
                    return Mono.error(new ResourceNotFoundException(errorMsg));
                }))
                .doOnSuccess(customer -> log.info("Customer validation successful for: {}", customer.getName()))
                .then();
    }

    @Override
    public Mono<Account> findAccountById(Long id) {
        log.info("Searching for account with id: {}", id);
        return accountRepositoryPort.findAccountById(id)
                .doOnSuccess(account -> {
                    if (account != null) {
                        log.info("Account found with id: {}", id);
                    } else {
                        log.warn("Account not found for id: {}", id);
                    }
                });
    }

    @Override
    public Mono<Void> deleteAccount(Long id) {
        log.info("Attempting to deactivate account with id: {}", id);
        return accountRepositoryPort.findAccountById(id)
                .flatMap(accountFound -> {
                    accountFound.setStatus(false);
                    return accountRepositoryPort.saveAccount(accountFound)
                            .doOnSuccess(acc -> log.info("Successfully deactivated account with id: {}", id));
                })
                .doOnError(throwable -> log.error("Failed to deactivate account with id: {}", id, throwable))
                .then();
    }

    @Override
    public Flux<Account> findAllAccounts() {
        log.info("Retrieving all accounts");
        return accountRepositoryPort.findAll()
                .doOnComplete(() -> log.info("All accounts retrieved successfully"));
    }

    @Override
    public Mono<Account> updateAccount(Long id, Account accountUpdateRequest) {
        log.info("Attempting to update account with id: {}", id);
        return accountRepositoryPort.findAccountById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Account not found with id: " + id)))
                .flatMap(existingAccount -> {

                    Mono<Void> validationMono = Mono.empty();

                    if (accountUpdateRequest.getAccountNumber() != null && !accountUpdateRequest.getAccountNumber().equals(existingAccount.getAccountNumber())) {
                        log.info("Account number change detected for account ID {}. Validating new number '{}'", id, accountUpdateRequest.getAccountNumber());
                        validationMono = validateUniqueAccountNumberForUpdate(accountUpdateRequest.getAccountNumber(), id);
                    }

                    return validationMono.then(Mono.defer(() -> {
                        existingAccount.setAccountNumber(accountUpdateRequest.getAccountNumber());
                        existingAccount.setAccountType(accountUpdateRequest.getAccountType());
                        existingAccount.setStatus(accountUpdateRequest.getStatus());

                        log.info("Updating account with ID: {}", id);
                        return accountRepositoryPort.saveAccount(existingAccount);
                    }));
                });
    }

    private Mono<Void> validateUniqueAccountNumberForUpdate(String accountNumber, Long accountId) {
        return accountRepositoryPort.findAccountByAccountNumberAndIdNot(accountNumber, accountId)
                .flatMap(existingAccount -> {
                    String errorMsg = "Cannot update. Another account with number '" + accountNumber + "' already exists.";
                    log.warn(errorMsg);
                    return Mono.error(new DuplicateResourceException(errorMsg));
                }).then();
    }
}