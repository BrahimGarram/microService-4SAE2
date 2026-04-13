package com.freelance.projectservice.exception;

/**
 * Thrown when the user does not have enough tokens for an action (e.g. create project, submit proposal).
 */
public class InsufficientTokensException extends RuntimeException {

    private final int required;

    public InsufficientTokensException(String message, int required) {
        super(message);
        this.required = required;
    }

    public int getRequired() {
        return required;
    }
}
