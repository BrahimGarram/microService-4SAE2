package com.freelance.projectservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * RestTemplate configuration (used for Ollama API calls).
 */
@Configuration
public class RestTemplateConfig {

    @Value("${ollama.timeout-seconds:60}")
    private int timeoutSeconds;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(Math.min(timeoutSeconds, 30)));
        factory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(timeoutSeconds));
        return new RestTemplate(factory);
    }
}
