package com.freelance.userservice.exception;

/**
 * Thrown when registration fails because a user with the same username or email already exists in Keycloak.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
