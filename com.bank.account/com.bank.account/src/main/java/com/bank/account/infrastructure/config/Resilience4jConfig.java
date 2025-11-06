package com.bank.account.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    @Bean
    public Resilience4jCircuitBreakerFactory resilience4jCircuitBreakerFactory() {
        // Configuración por defecto para todos los Circuit Breakers
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10) // 10 peticiones
                .failureRateThreshold(50.0f) // 50% de fallos para abrir el circuito
                .waitDurationInOpenState(Duration.ofSeconds(10)) // Espera 10s antes de pasar a semi-abierto
                .permittedNumberOfCallsInHalfOpenState(2) // 2 peticiones en estado semi-abierto
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3)) // Timeout de 3 segundos
                .build();

        Resilience4jConfigBuilder builder = new Resilience4jConfigBuilder("default");
        builder.circuitBreakerConfig(circuitBreakerConfig);
        builder.timeLimiterConfig(timeLimiterConfig);

        return new Resilience4jCircuitBreakerFactory(builder);
    }
}