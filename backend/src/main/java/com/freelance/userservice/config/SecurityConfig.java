package com.freelance.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security Configuration
 * 
 * Configures the application as an OAuth2 Resource Server.
 * Validates JWT tokens from Keycloak and enables role-based authorization.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    /**
     * Security Filter Chain
     * 
     * Configures:
     * - OAuth2 Resource Server with JWT validation
     * - Session management (stateless)
     * - CORS configuration for Angular frontend
     * - Endpoint security rules
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Session management - stateless (JWT tokens)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/forgot-password").permitAll()
                .requestMatchers("/api/users/reset-password").permitAll()
                .requestMatchers("/uploads/**").permitAll() // Allow access to uploaded files
                
                // Admin only endpoints (must come before more general patterns)
                .requestMatchers("/api/users").hasRole("ADMIN")
                
                // Public browsing endpoint (authenticated users can browse public profiles)
                .requestMatchers("/api/users/public").authenticated()
                
                // Authenticated endpoints (require JWT token)
                .requestMatchers("/api/users/sync").authenticated() // Needs JWT to extract user info
                .requestMatchers("/api/users/me").authenticated()
                .requestMatchers("/api/users/{id}").authenticated() // GET (view profile), PUT (update), DELETE uses @PreAuthorize
                .requestMatchers("/api/users/username/{username}").authenticated() // View profile by username
                .requestMatchers("/api/users/{id}/profile-picture").authenticated() // Profile picture upload
                .requestMatchers("/api/users/{id}/ratings").authenticated() // Ratings endpoints
                .requestMatchers("/api/users/ratings/**").authenticated() // Rating management
                .requestMatchers("/api/messages/**").authenticated() // Messaging endpoints
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    /**
     * CORS Configuration
     * 
     * Allows Angular frontend (http://localhost:4200) to access the API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow Angular frontend origin
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow common headers including Authorization for JWT
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * JWT Authentication Converter
     * 
     * Converts Keycloak JWT roles to Spring Security authorities.
     * Extracts roles from 'realm_access.roles' and adds 'ROLE_' prefix.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtAuthConverter());
        return converter;
    }
}
