package com.sss.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * First outbound HTTP client this app needs (Meta Graph API calls) — no
 * RestTemplate/WebClient existed anywhere before. RestClient is Spring 6.1's
 * synchronous client, already on the classpath via spring-boot-starter-web,
 * so this needs no new Maven dependency.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
