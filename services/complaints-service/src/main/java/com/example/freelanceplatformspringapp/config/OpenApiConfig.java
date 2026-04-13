package com.example.freelanceplatformspringapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI complaintsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Complaints & Penalties API")
                        .description("Réclamations / pénalités — context-path /freelancity")
                        .version("1.0.0"));
    }
}
