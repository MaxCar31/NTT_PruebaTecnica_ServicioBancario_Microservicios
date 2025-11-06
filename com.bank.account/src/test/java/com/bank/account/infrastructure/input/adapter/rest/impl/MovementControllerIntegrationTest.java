package com.bank.account.infrastructure.input.adapter.rest.impl;

import com.bank.account.application.input.port.MovementUseCase;
import com.bank.account.domain.exception.InsufficientBalanceException;
import com.bank.account.domain.model.Movement;
import com.bank.account.infrastructure.exception.ErrorResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.bank.account.infrastructure.input.adapter.rest.dto.request.MovementRequest;
import com.bank.account.infrastructure.input.adapter.rest.dto.response.MovementResponse;
import com.bank.account.infrastructure.input.adapter.rest.mapper.MovementRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;



@WebFluxTest(controllers = MovementController.class)
@Import(com.bank.account.infrastructure.input.adapter.rest.mapper.MovementRestMapperImpl.class)
@DisplayName("Movement Controller Integration Tests")
@ExtendWith(SpringExtension.class)
class MovementControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public MovementUseCase movementUseCase() {
            return Mockito.mock(MovementUseCase.class);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovementUseCase movementUseCase;

    @Autowired
    private MovementRestMapper movementRestMapper;

    private MovementRequest movementRequest;
    private Movement domainMovement;
    private MovementResponse movementResponse;

    @BeforeEach
    void setUp() {
        // 1. DTO de Petición (Lo que envía el cliente)
        movementRequest = new MovementRequest();
        movementRequest.setAccountId(1L);
        movementRequest.setAmount(new BigDecimal("-100.00"));

        // 2. Modelo de Dominio (Lo que devuelve el UseCase)
        domainMovement = Movement.builder()
                .id(1L)
                .accountId(1L)
                .amount(new BigDecimal("-100.00"))
                .balance(new BigDecimal("900.00"))
                .movementType("Debit")
                .date(LocalDateTime.now())
                .build();

        // 3. DTO de Respuesta (Lo que esperamos recibir)
        movementResponse = movementRestMapper.toResponse(domainMovement);
    }

    @Test
    @DisplayName("should register movement successfully and return 201 Created")
    void shouldRegisterMovementSuccessfully() {
        // Arrange
        when(movementUseCase.registerMovement(any(Movement.class))).thenReturn(Mono.just(domainMovement));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movementRequest)
                .exchange()
                .expectStatus().isCreated() // HTTP 201
                .expectBody(MovementResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isEqualTo(1L);
                    assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
                    assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("900.00"));
                });
    }

    @Test
    @DisplayName("should fail registration if use case throws InsufficientBalanceException and return 400 Bad Request")
    void shouldFailOnInsufficientBalance() {
        // Arrange
        // Simulamos que el caso de uso lanza la excepción de negocio
        when(movementUseCase.registerMovement(any(Movement.class)))
                .thenReturn(Mono.error(new InsufficientBalanceException("Saldo no disponible")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movementRequest)
                .exchange()
                .expectStatus().isBadRequest() // HTTP 400
                .expectBody(ErrorResponse.class)
                .value(error -> {
                    assertThat(error.getStatus()).isEqualTo(400);
                    assertThat(error.getError()).isEqualTo("Bad Request");
                    assertThat(error.getMessage()).isEqualTo("Saldo no disponible");
                });
    }

    @Test
    @DisplayName("should fail validation if amount is null and return 400 Bad Request")
    void shouldFailValidationOnNullAmount() {
        // Arrange
        MovementRequest invalidRequest = new MovementRequest();
        invalidRequest.setAccountId(1L);
        invalidRequest.setAmount(null); // Esto viola la anotación @NotNull

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest() // HTTP 400
                .expectBody(ErrorResponse.class)
                .value(error -> {
                    assertThat(error.getStatus()).isEqualTo(400);
                    assertThat(error.getError()).isEqualTo("Validation Error");
                    assertThat(error.getMessage()).contains("Movement amount cannot be null");
                });
    }
}