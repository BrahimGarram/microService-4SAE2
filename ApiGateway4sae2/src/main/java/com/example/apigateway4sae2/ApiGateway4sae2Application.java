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
                /* Service Test: context-path = /service-test (voir application.properties du service-test) */
                .route("service-test", r -> r.path("/service-test/**").uri("lb://SERVICE-TEST"))
                /* User service: pas de context-path; controllers sous /api/users, /api/posts, /api/messages, /api/follow */
                .route("user-service", r -> r.path("/user", "/user/**")
                        .filters(f -> f.rewritePath("/user(?<path>.*)", "/api/users${path}"))
                        .uri("lb://USER-SERVICE"))
                .route("user-posts", r -> r.path("/api/posts/**").uri("lb://USER-SERVICE"))
                .route("user-messages", r -> r.path("/api/messages/**").uri("lb://USER-SERVICE"))
                .route("user-follow", r -> r.path("/api/follow/**").uri("lb://USER-SERVICE"))
                .route("user-group-chats", r -> r.path("/api/group-chats/**").uri("lb://USER-SERVICE"))
                .route("user-chat", r -> r.path("/api/chat/**").uri("lb://USER-SERVICE"))
                .route("user-notifications", r -> r.path("/api/notifications/**").uri("lb://USER-SERVICE"))
                .route("user-ws", r -> r.path("/ws/**").uri("lb://USER-SERVICE"))
                /* Project service: pas de context-path; controllers sous /api/projects */
                .route("project-service", r -> r.path("/project", "/project/**")
                        .filters(f -> f.rewritePath("/project(?<path>.*)", "/api/projects${path}"))
                        .uri("lb://PROJECT-SERVICE"))
                .route("project-proposals", r -> r.path("/api/proposals/**").uri("lb://PROJECT-SERVICE"))
                /* Project room tasks: path dédié pour éviter conflit avec collaboration-service (workspace = /collaboration/api/tasks) */
                .route("project-tasks", r -> r.path("/api/project-tasks", "/api/project-tasks/**")
                        .filters(f -> f.rewritePath("/api/project-tasks(?<path>.*)", "/api/tasks${path}"))
                        .uri("lb://PROJECT-SERVICE"))
                /* Complaints service: context-path = /freelancity */
                .route("complaints-service", r -> r.path("/complaints/**")
                        .filters(f -> f.rewritePath("/complaints/(?<path>.*)", "/freelancity/${path}"))
                        .uri("lb://FREELANCE-PLATFORM-SPRING-APP"))
                .build();
    }
}
