package com.freelance.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * RestTemplate for Ollama API calls (long timeout for LLM response).
 */
@Configuration
public class OllamaConfig {

    @Value("${ollama.timeout-seconds:60}")
    private int timeoutSeconds;

    @Bean(name = "ollamaRestTemplate")
    public RestTemplate ollamaRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(Math.min(10, timeoutSeconds)));
        factory.setReadTimeout((int) TimeUnit.SECONDS.toMillis(timeoutSeconds));
        return new RestTemplate(factory);
    }
}
