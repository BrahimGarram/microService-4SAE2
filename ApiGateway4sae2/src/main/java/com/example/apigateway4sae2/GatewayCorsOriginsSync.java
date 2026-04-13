package com.example.apigateway4sae2;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Aligne {@code spring.cloud.gateway.globalcors} avec {@code gateway.cors.allowed-origins}
 * (dont {@code CORS_ALLOWED_ORIGINS}) pour que le prévol OPTIONS du Gateway accepte les mêmes
 * origines que {@link CorsFixGlobalFilter}.
 */
@Configuration
public class GatewayCorsOriginsSync implements InitializingBean {

    private final GlobalCorsProperties globalCorsProperties;
    private final String allowedOriginsCsv;

    public GatewayCorsOriginsSync(
            GlobalCorsProperties globalCorsProperties,
            @Value("${gateway.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200,http://[::1]:4200}") String allowedOriginsCsv) {
        this.globalCorsProperties = globalCorsProperties;
        this.allowedOriginsCsv = allowedOriginsCsv;
    }

    @Override
    public void afterPropertiesSet() {
        List<String> origins = CorsAllowedOrigins.asList(allowedOriginsCsv);
        // Spring Cloud Gateway lie la clé YAML '[/**]' telle quelle — ne pas utiliser seulement "/**".
        globalCorsProperties.getCorsConfigurations().forEach((path, cfg) -> cfg.setAllowedOrigins(origins));
    }
}
