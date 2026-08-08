package com.allobank.splitbill.exception;

// Custom exception thrown when a requested resource is not found in the database
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
