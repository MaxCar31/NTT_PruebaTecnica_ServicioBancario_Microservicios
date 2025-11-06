package com.bank.account.infrastructure.exception;

import com.bank.account.domain.exception.DuplicateResourceException;
import com.bank.account.domain.exception.InsufficientBalanceException;
import com.bank.account.domain.exception.ResourceNotFoundException;
import com.bank.account.domain.exception.FileGenerationException;
import com.bank.account.domain.exception.CustomerServiceException; // NUEVO
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles the specific exception for "Insufficient Balance".
     * Returns an HTTP 400 (Bad Request) status.
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInsufficientBalance(InsufficientBalanceException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    /**
     * Handles validation errors for DTOs (@Valid).
     * Returns an HTTP 400 (Bad Request) status with validation details.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationExceptions(WebExchangeBindException ex) {
        String errors = ex.getBindingResult()
                .getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                errors
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST));
    }

    /**
     * Handles failures communicating with the external Customer service.
     * Returns an HTTP 503 (Service Unavailable) status.
     */
    @ExceptionHandler(CustomerServiceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomerServiceException(CustomerServiceException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "Customer service is currently unavailable. " + ex.getMessage()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE));
    }

    /**
     * Handles the specific exception for "File Generation Error".
     * Returns an HTTP 500 (Internal Server Error) status.
     */
    @ExceptionHandler(FileGenerationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleFileGenerationException(FileGenerationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "File Generation Error",
                "An unexpected error occurred while generating the file."
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * Handles the specific exception for "Resource Not Found".
     * Returns an HTTP 404 (Not Found) status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND));
    }

    /**
     * Handles the specific exception for duplicate resources.
     * Returns an HTTP 409 (Conflict) status.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateResource(DuplicateResourceException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage()
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT));
    }

    /**
     * Handles any other unhandled exceptions.
     * Returns an HTTP 500 (Internal Server Error) status.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        // Log the full exception for debugging
        // log.error("Unhandled exception occurred: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support."
        );
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}