package com.bank.account.domain.exception;

public class FileGenerationException extends RuntimeException {
    public FileGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}