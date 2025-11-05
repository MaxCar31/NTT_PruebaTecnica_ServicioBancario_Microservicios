package com.bank.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Account Service microservice.
 * Manages accounts, movements, ledger entries and reports.
 * Communicates with customer-service for customer validation.
 */
@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
