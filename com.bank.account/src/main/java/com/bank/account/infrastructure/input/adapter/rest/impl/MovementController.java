package com.bank.account.infrastructure.input.adapter.rest.impl;

import com.bank.account.application.input.port.MovementUseCase;
import com.bank.account.infrastructure.exception.ErrorResponse;
import com.bank.account.infrastructure.input.adapter.rest.dto.request.MovementRequest;
import com.bank.account.infrastructure.input.adapter.rest.dto.response.MovementResponse;
import com.bank.account.infrastructure.input.adapter.rest.mapper.MovementRestMapper;
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
@RequestMapping("/api/v1/movements")
@RequiredArgsConstructor
@Tag(name = "Movements", description = "Endpoints for registering and managing account movements")
public class MovementController {

    private final MovementUseCase movementUseCase;
    private final MovementRestMapper movementRestMapper;

    @PostMapping
    @Operation(summary = "Register a new movement", description = "Registers a new credit or debit movement in an account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movement registered successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid data.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<MovementResponse>> registerMovement(@Valid @RequestBody MovementRequest movementRequest) {
        return movementUseCase.registerMovement(movementRestMapper.toDomain(movementRequest))
                .map(movement -> ResponseEntity.status(HttpStatus.CREATED).body(movementRestMapper.toResponse(movement)));
    }

    @GetMapping("/by-account")
    @Operation(summary = "Get all movements for a specific account", description = "Returns a list of all movements for a given account ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of movements retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = MovementResponse.class))))
    })
    public Flux<MovementResponse> findMovementsByAccountId(
            @Parameter(description = "ID of the account whose movements are to be retrieved.", required = true) @RequestParam Long accountId) {
        return movementUseCase.findMovementsByAccountId(accountId)
                .map(movementRestMapper::toResponse);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all movements", description = "Returns a list of all movements in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all movements retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = MovementResponse.class))))
    })
    public Flux<MovementResponse> findAllMovements() {
        return movementUseCase.findAllMovements()
                .map(movementRestMapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a movement by ID", description = "Returns a single movement by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movement found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MovementResponse.class))),
            @ApiResponse(responseCode = "404", description = "Movement not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<MovementResponse>> findMovementById(
            @Parameter(description = "ID of the movement to be obtained.", required = true) @PathVariable Long id) {
        return movementUseCase.findMovementById(id)
                .map(movement -> ResponseEntity.ok(movementRestMapper.toResponse(movement)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movement by ID", description = "Deletes a movement from the system by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Movement deleted successfully.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Movement not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<Void>> deleteMovement(
            @Parameter(description = "ID of the movement to be deleted.", required = true) @PathVariable Long id) {
        return movementUseCase.deleteMovement(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}