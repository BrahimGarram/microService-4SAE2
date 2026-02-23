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
                .route("service-test", r->r.path("/service-test/**")
                        .uri("lb://SERVICE-TEST"))
                .route("collaboration-service", r -> r.path("/collaboration/**")
                        .filters(f -> f.rewritePath("/collaboration/(?<path>.*)", "/$\\{path}"))
                        .uri("lb://COLLABORATION-SERVICE"))
                .route("project-service", r -> r.path("/project/**")
                        .filters(f -> f.rewritePath("/project/(?<path>.*)", "/${path}"))
                        .uri("lb://PROJECT-SERVICE"))
                .build();
    }
}
