package com.bank.account.domain.exception;

/**
 * Custom exception for errors occurring during communication
 * with the Customer microservice.
 */
public class CustomerServiceException extends RuntimeException {
    public CustomerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}