package com.example.servicetest.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse {
    private final List<FieldError> errors = new ArrayList<>();

    public List<FieldError> getErrors() {
        return errors;
    }

    public void addError(String field, String message) {
        this.errors.add(new FieldError(field, message));
    }

    public static class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
