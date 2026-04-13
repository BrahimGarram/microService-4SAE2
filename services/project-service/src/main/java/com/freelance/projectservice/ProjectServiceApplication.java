package com.freelance.projectservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Project Microservice
 * 
 * This microservice handles project management (CRUD operations).
 * No authentication - authentication is handled by user-service.
 */
@SpringBootApplication
public class ProjectServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}
