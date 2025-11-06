package com.bank.customer.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;


@Configuration
@EnableWebFluxSecurity 
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        
        http
          
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            .authorizeExchange(exchanges -> exchanges
                
              
                .pathMatchers("/", "/swagger-ui.html", "/webjars/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/v1/customers/**").authenticated()
                .anyExchange().authenticated() 
            )

            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}