package com.freelance.userservice.model;

/**
 * User Role Enumeration
 * 
 * Defines the available roles in the system:
 * - USER: Regular user
 * - FREELANCER: Freelancer user
 * - ADMIN: Administrator
 * 
 * These roles must match the roles defined in Keycloak realm.
 */
public enum UserRole {
    USER,
    FREELANCER,
    ADMIN
}
