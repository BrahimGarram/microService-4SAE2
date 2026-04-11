package com.example.apigateway4sae2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;


@SpringBootApplication
@EnableDiscoveryClient

public class ApiGateway4sae2Application {

    public static void main(String[] args) {
        SpringApplication.run(ApiGateway4sae2Application.class, args);
    }
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){

        return builder.routes()
                .route("Condidat", r->r.path("/candidats/**")
                        .uri("http://localhost:8089"))
                .build();
    }
}
