package com.bank.account.application.service;

import com.bank.account.application.input.port.ReportUseCase;
import com.bank.account.application.output.port.AccountRepositoryPort;
import com.bank.account.application.output.port.CustomerClientPort;
import com.bank.account.application.output.port.LedgerRepositoryPort;
import com.bank.account.domain.exception.ResourceNotFoundException;
import com.bank.account.domain.model.Account;
import com.bank.account.domain.model.AccountStatement;
import com.bank.account.domain.model.Customer;
import com.bank.account.domain.model.LedgerEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating account statements.
 * Uses the {@link LedgerRepositoryPort} as the single source of truth to ensure
 * statements are based on immutable historical records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportUseCaseImpl implements ReportUseCase {

    private final CustomerClientPort customerClientPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final LedgerRepositoryPort ledgerRepositoryPort;

    /**
     * Internal data container that aggregates base report information.
     */
    private record ReportData(Customer customer, List<Account> accounts) {}

    @Override
    public Mono<AccountStatement> generateAccountStatement(Long clientId, String accountNumber,
                                                           LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating statement for clientId={}, accountNumber={}, from {} to {}",
                clientId, accountNumber, startDate, endDate);

        return fetchReportData(clientId, accountNumber)
                .flatMap(reportData -> {
                    if (reportData.accounts.isEmpty()) {
                        log.warn("No accounts found for clientId={}, accountNumber={}", clientId, accountNumber);
                        return Mono.just(buildEmptyStatement(reportData.customer, startDate, endDate));
                    }

                    List<Long> accountIds = reportData.accounts.stream().map(Account::getId).toList();

                    return ledgerRepositoryPort.findByAccountIdsAndDateRange(accountIds, startDate, endDate)
                            .collectList()
                            .map(entries -> {
                                log.info("Retrieved {} ledger entries for statement", entries.size());
                                return buildFullStatementFromLedger(reportData, entries, startDate, endDate);
                            });
                });
    }

    /**
     * Retrieves customer and account data in parallel.
     * If only the account number is provided, the customer is inferred from it.
     */
    private Mono<ReportData> fetchReportData(Long clientId, String accountNumber) {
        Mono<List<Account>> accountsMono = (accountNumber != null && !accountNumber.isBlank())
                ? accountRepositoryPort.findAccountByNumber(accountNumber)
                .map(List::of)
                .switchIfEmpty(Mono.just(Collections.emptyList()))
                : accountRepositoryPort.findByCustomerId(clientId).collectList();

        Mono<Customer> customerMono = (clientId != null)
                ? customerClientPort.findCustomerById(clientId)
                : accountRepositoryPort.findAccountByNumber(accountNumber)
                .flatMap(account -> customerClientPort.findCustomerById(account.getCustomerId()));

        return Mono.zip(customerMono, accountsMono, ReportData::new)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Client or account not found with the given parameters.")));
    }

    /**
     * Builds a complete statement grouped by account.
     */
    private AccountStatement buildFullStatementFromLedger(ReportData reportData,
                                                          List<LedgerEntry> allLedgerEntries,
                                                          LocalDateTime startDate,
                                                          LocalDateTime endDate) {
        Map<Long, List<LedgerEntry>> entriesByAccount = allLedgerEntries.stream()
                .collect(Collectors.groupingBy(LedgerEntry::getAccountId));

        List<AccountStatement.AccountReportDetail> accountDetails = reportData.accounts.stream()
                .map(account -> {
                    List<LedgerEntry> accountEntries = entriesByAccount.getOrDefault(account.getId(), Collections.emptyList());
                    return buildAccountDetailFromLedger(account, accountEntries, startDate, endDate);
                })
                .toList();

        return AccountStatement.builder()
                .clientName(reportData.customer.getName())
                .startDate(startDate)
                .endDate(endDate)
                .accounts(accountDetails)
                .build();
    }

    /**
     * Builds the detailed report for a single account, including balances and movements.
     */
    private AccountStatement.AccountReportDetail buildAccountDetailFromLedger(Account account,
                                                                              List<LedgerEntry> ledgerEntries,
                                                                              LocalDateTime startDate,
                                                                              LocalDateTime endDate) {
        ledgerEntries.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));

        BigDecimal initialBalance = calculateInitialBalanceForPeriod(account, ledgerEntries, startDate);
        BigDecimal finalBalance = ledgerEntries.isEmpty()
                ? initialBalance
                : ledgerEntries.get(ledgerEntries.size() - 1).getBalanceAfter();

        List<AccountStatement.MovementReportDetail> movements = ledgerEntries.stream()
                .map(this::convertLedgerEntryToMovementDetail)
                .toList();

        log.debug("Account {}: {} entries, initial={}, final={}",
                account.getAccountNumber(), movements.size(), initialBalance, finalBalance);

        return AccountStatement.AccountReportDetail.builder()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .initialBalance(initialBalance)
                .finalBalance(finalBalance)
                .movements(movements)
                .build();
    }

    /**
     * Determines the initial balance for a given period.
     * - If there are transactions, uses the first entry’s balanceBefore.
     * - Otherwise, uses the account’s current balance.
     */
    private BigDecimal calculateInitialBalanceForPeriod(Account account,
                                                        List<LedgerEntry> entriesInPeriod,
                                                        LocalDateTime startDate) {
        if (!entriesInPeriod.isEmpty()) {
            return entriesInPeriod.get(0).getBalanceBefore();
        }

        log.warn("No ledger entries found in period for account {}. Using current balance as initial.", account.getId());
        return account.getInitialBalance();
    }

    /**
     * Converts a ledger entry into a customer-facing movement detail.
     * Debits are displayed as negative amounts.
     */
    private AccountStatement.MovementReportDetail convertLedgerEntryToMovementDetail(LedgerEntry ledgerEntry) {
        String movementType = ledgerEntry.getEntryType() == LedgerEntry.LedgerEntryType.DEBIT ? "Debit" : "Credit";
        BigDecimal amount = ledgerEntry.getEntryType() == LedgerEntry.LedgerEntryType.DEBIT
                ? ledgerEntry.getAmount().negate()
                : ledgerEntry.getAmount();

        return AccountStatement.MovementReportDetail.builder()
                .date(ledgerEntry.getTimestamp())
                .movementType(movementType)
                .amount(amount)
                .balanceAfterMovement(ledgerEntry.getBalanceAfter())
                .build();
    }

    /**
     * Builds an empty statement when the customer has no accounts.
     */
    private AccountStatement buildEmptyStatement(Customer customer, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Building empty statement for customer: {}", customer.getName());
        return AccountStatement.builder()
                .clientName(customer.getName())
                .startDate(startDate)
                .endDate(endDate)
                .accounts(Collections.emptyList())
                .build();
    }
}
