package com.freelance.collaborationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Spring Boot Application for Collaboration Microservice.
 * Handles companies, collaborations, and collaboration requests.
 */
@EnableFeignClients
@SpringBootApplication
public class CollaborationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollaborationServiceApplication.class, args);
    }
}
