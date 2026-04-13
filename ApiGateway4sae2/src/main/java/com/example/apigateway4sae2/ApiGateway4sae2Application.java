package com.example.apigateway4sae2;

import org.springframework.beans.factory.annotation.Value;
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
    /**
     * URIs par défaut {@code lb://...} (Eureka). En Docker Compose, surcharger via les variables
     * {@code GATEWAY_USER_SERVICE_URI}, {@code GATEWAY_SERVICE_TEST_URI} pour éviter les 503
     * si le registre Eureka est vide ou lent. Collaboration (Symfony) : toujours une URI HTTP
     * directe ({@code GATEWAY_COLLABORATION_URI}) — pas d'enregistrement Eureka.
     */
    @Bean
    public RouteLocator gatewayRoutes(
            RouteLocatorBuilder builder,
            @Value("${gateway.user-service.uri:lb://USER-SERVICE}") String userServiceUri,
            @Value("${gateway.service-test.uri:lb://SERVICE-TEST}") String serviceTestUri,
            @Value("${gateway.collaboration.uri:http://localhost:8888}") String collaborationUri,
            @Value("${gateway.project-service.uri:lb://PROJECT-SERVICE}") String projectServiceUri,
            @Value("${gateway.complaints.uri:lb://FREELANCE-PLATFORM-SPRING-APP}") String complaintsUri) {

        return builder.routes()
                /* OpenAPI / Swagger UI du user-service (sans réécriture /user -> /api/users) */
                .route("user-openapi", r -> r.path("/user/v3/api-docs/**", "/user/swagger-ui/**", "/user/swagger-ui.html")
                        .filters(f -> f.rewritePath("/user/(?<path>.*)", "/${path}"))
                        .uri(userServiceUri))
                /* Service Test: context-path = /service-test (voir application.properties du service-test) */
                .route("service-test", r -> r.path("/service-test/**").uri(serviceTestUri))
                /* User service: pas de context-path; controllers sous /api/users, /api/posts, /api/messages, /api/follow */
                .route("user-service", r -> r.path("/user", "/user/**")
                        .filters(f -> f.rewritePath("/user(?<path>.*)", "/api/users${path}"))
                        .uri(userServiceUri))
                .route("user-posts", r -> r.path("/api/posts/**").uri(userServiceUri))
                .route("user-messages", r -> r.path("/api/messages/**").uri(userServiceUri))
                .route("user-follow", r -> r.path("/api/follow/**").uri(userServiceUri))
                .route("user-group-chats", r -> r.path("/api/group-chats/**").uri(userServiceUri))
                .route("user-chat", r -> r.path("/api/chat/**").uri(userServiceUri))
                .route("user-notifications", r -> r.path("/api/notifications/**").uri(userServiceUri))
                .route("user-ws", r -> r.path("/ws/**").uri(userServiceUri))
                /* Project service: pas de context-path; controllers sous /api/projects */
                .route("project-service", r -> r.path("/project", "/project/**")
                        .filters(f -> f.rewritePath("/project(?<path>.*)", "/api/projects${path}"))
                        .uri(projectServiceUri))
                .route("project-proposals", r -> r.path("/api/proposals/**").uri(projectServiceUri))
                /* Project room tasks: path dédié pour éviter conflit avec collaboration-service (workspace = /collaboration/api/tasks) */
                .route("project-tasks", r -> r.path("/api/project-tasks", "/api/project-tasks/**")
                        .filters(f -> f.rewritePath("/api/project-tasks(?<path>.*)", "/api/tasks${path}"))
                        .uri(projectServiceUri))
                /* Complaints service: context-path = /freelancity */
                .route("complaints-service", r -> r.path("/complaints/**")
                        .filters(f -> f.rewritePath("/complaints/(?<path>.*)", "/freelancity/${path}"))
                        .uri(complaintsUri))
                /* Symfony collaboration (pubs): /collaboration/api/... -> service */
                .route("collaboration-service", r -> r.path("/collaboration/**")
                        .filters(f -> f.rewritePath("/collaboration/(?<path>.*)", "/${path}"))
                        .uri(collaborationUri))
                .build();
    }
}
