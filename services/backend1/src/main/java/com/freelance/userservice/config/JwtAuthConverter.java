package com.freelance.userservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Authentication Converter
 * 
 * Converts Keycloak JWT tokens to Spring Security authorities.
 * Extracts roles from 'realm_access.roles' claim and converts them
 * to Spring Security GrantedAuthority objects with 'ROLE_' prefix.
 */
public class JwtAuthConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Extract realm_access.roles from JWT
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        
        if (realmAccess == null) {
            return List.of();
        }
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        
        // Convert roles to Spring Security authorities with ROLE_ prefix
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }
}
