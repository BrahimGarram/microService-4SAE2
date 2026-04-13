package com.example.servicetest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceTestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Service Test API")
                        .description("Tests & affectations — context-path /service-test")
                        .version("1.0.0"));
    }
}
