package com.example.apigateway4sae2;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGatewayConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway")
                        .description("Point d'entrée. Swagger des microservices : /user/swagger-ui/... (user-service), /service-test/swagger-ui/... (service-test), /complaints/swagger-ui/... (complaints).")
                        .version("1.0.0"));
    }
}
