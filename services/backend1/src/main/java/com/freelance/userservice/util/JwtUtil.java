package com.freelance.userservice.util;

import com.freelance.userservice.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Utility Class
 * 
 * Provides helper methods to extract information from JWT tokens.
 * Keycloak JWT tokens contain user information in specific claims.
 */
public class JwtUtil {
    
    /**
     * Extract Keycloak user ID (sub claim) from JWT
     * @param jwt The JWT token
     * @return Keycloak user ID
     */
    public static String extractKeycloakId(Jwt jwt) {
        return jwt.getSubject();
    }
    
    /**
     * Extract username from JWT
     * Uses 'preferred_username' claim from Keycloak
     * @param jwt The JWT token
     * @return Username
     */
    public static String extractUsername(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }
    
    /**
     * Extract email from JWT
     * @param jwt The JWT token
     * @return Email address
     */
    public static String extractEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }
    
    /**
     * Extract roles from JWT
     * Keycloak roles are in 'realm_access.roles' claim
     * @param jwt The JWT token
     * @return List of role names
     */
    @SuppressWarnings("unchecked")
    public static List<String> extractRoles(Jwt jwt) {
        // Keycloak stores roles in realm_access.roles
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map) {
            java.util.Map<String, Object> realmAccessMap = (java.util.Map<String, Object>) realmAccess;
            Object roles = realmAccessMap.get("roles");
            if (roles instanceof List) {
                return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            }
        }
        return List.of();
    }
    
    /**
     * Extract the highest role from JWT
     * Priority: ADMIN > ENTERPRISE > FREELANCER > USER
     * @param jwt The JWT token
     * @return UserRole enum
     */
    public static UserRole extractUserRole(Jwt jwt) {
        List<String> roles = extractRoles(jwt);
        
        if (roles.contains("ADMIN")) {
            return UserRole.ADMIN;
        } else if (roles.contains("ENTERPRISE")) {
            return UserRole.ENTERPRISE;
        } else if (roles.contains("FREELANCER")) {
            return UserRole.FREELANCER;
        } else {
            return UserRole.USER;
        }
    }
    
    /**
     * Convert JWT roles to Spring Security authorities
     * @param jwt The JWT token
     * @return Collection of GrantedAuthority
     */
    public static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return extractRoles(jwt).stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }
}
