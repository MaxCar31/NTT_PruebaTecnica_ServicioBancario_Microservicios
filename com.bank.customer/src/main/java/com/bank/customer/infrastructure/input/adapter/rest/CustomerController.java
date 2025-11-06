package com.bank.customer.infrastructure.input.adapter.rest.impl;

import com.bank.customer.infrastructure.input.adapter.rest.dto.request.CreateCustomerRequest;
import com.bank.customer.infrastructure.input.adapter.rest.dto.request.UpdateCustomerRequest;
import com.bank.customer.application.input.port.CustomerUseCase;
import com.bank.customer.infrastructure.exception.ErrorResponse;
import com.bank.customer.infrastructure.input.adapter.rest.dto.response.CustomerResponse;
import com.bank.customer.infrastructure.input.adapter.rest.mapper.CustomerRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Endpoints for customer management")
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final CustomerRestMapper customerRestMapper;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Allows registering a new customer in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest createRequest) {

        return customerUseCase.createCustomer(customerRestMapper.toDomain(createRequest))
                .map(customer -> ResponseEntity.status(HttpStatus.CREATED).body(customerRestMapper.toResponse(customer)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by ID", description = "Returns a single customer by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<CustomerResponse>> findCustomerById(
            @Parameter(description = "ID of the customer to be obtained.", required = true) @PathVariable Long id) {
        return customerUseCase.findCustomerById(id)
                .map(customer -> ResponseEntity.ok(customerRestMapper.toResponse(customer)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Returns a list of all registered customers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of customers retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class))))
    })
    public Flux<CustomerResponse> findAllCustomers() {
        return customerUseCase.findAllCustomers()
                .map(customerRestMapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer", description = "Allows updating the data of an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(
            @Parameter(description = "ID of the customer to be updated.", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest updateRequest) {
        return customerUseCase.updateCustomer(id, customerRestMapper.toDomain(updateRequest))
                .map(customer -> ResponseEntity.ok(customerRestMapper.toResponse(customer)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer by ID", description = "Logically deletes a customer from the system by their ID by changing their status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<Void>> deleteCustomer(
            @Parameter(description = "ID of the customer to be deleted.", required = true) @PathVariable Long id) {
        return customerUseCase.deleteCustomer(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}