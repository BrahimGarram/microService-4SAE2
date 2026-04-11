package com.freelance.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for User Microservice
 * 
 * This microservice handles user profile management and integrates with Keycloak
 * for authentication. It stores user profile data in MySQL database.
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
