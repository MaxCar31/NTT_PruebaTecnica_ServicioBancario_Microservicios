package com.bank.account.infrastructure.input.adapter.rest.impl;

import com.bank.account.application.input.port.AccountUseCase;
import com.bank.account.infrastructure.exception.ErrorResponse;
import com.bank.account.infrastructure.input.adapter.rest.dto.request.AccountRequest;
import com.bank.account.infrastructure.input.adapter.rest.dto.response.AccountResponse;
import com.bank.account.infrastructure.input.adapter.rest.mapper.AccountRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints for bank account management")
public class AccountController {

    private final AccountUseCase accountUseCase;
    private final AccountRestMapper accountRestMapper;

    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new bank account for an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Associated customer not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Customer service is unavailable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AccountResponse>> createAccount(@Valid @RequestBody AccountRequest accountRequest) {
        return accountUseCase.createAccount(accountRestMapper.toDomain(accountRequest))
                .map(account -> ResponseEntity.status(HttpStatus.CREATED).body(accountRestMapper.toResponse(account)));
    }

    @GetMapping("/search")
    @Operation(summary = "Get an account by its number", description = "Returns a single account by its unique account number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AccountResponse>> findAccountByNumber(
            @Parameter(description = "Account number to search for.", required = true) @RequestParam String accountNumber) {
        return accountUseCase.findAccountByNumber(accountNumber)
                .map(account -> ResponseEntity.ok(accountRestMapper.toResponse(account)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an account by ID", description = "Returns a single account by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AccountResponse>> findAccountById(
            @Parameter(description = "ID of the account to be obtained.", required = true) @PathVariable Long id) {
        return accountUseCase.findAccountById(id)
                .map(account -> ResponseEntity.ok(accountRestMapper.toResponse(account)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Returns a list of all registered accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of accounts retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class))))
    })
    public Flux<AccountResponse> findAllAccounts() {
        return accountUseCase.findAllAccounts()
                .map(accountRestMapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing account", description = "Allows updating the data of an existing account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<AccountResponse>> updateAccount(
            @Parameter(description = "ID of the account to be updated.", required = true) @PathVariable Long id,
            @Valid @RequestBody AccountRequest accountRequest) {
        return accountUseCase.updateAccount(id, accountRestMapper.toDomain(accountRequest))
                .map(account -> ResponseEntity.ok(accountRestMapper.toResponse(account)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an account by ID", description = "Logically deletes a bank account by its ID by changing its status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<Void>> deleteAccount(
            @Parameter(description = "ID of the account to be deleted.", required = true) @PathVariable Long id) {
        return accountUseCase.deleteAccount(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}