package com.bank.account.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.customer.base-url}")
    private String customerServiceBaseUrl;

    @Bean
    public WebClient customerWebClient(WebClient.Builder builder) {
        return builder.baseUrl(customerServiceBaseUrl).build();
    }
}