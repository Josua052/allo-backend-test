package com.allobank.splitbill.exception;

// Custom exception thrown when a participant is not found or does not belong to the target group
public class InvalidParticipantException extends RuntimeException {
    public InvalidParticipantException(String message) {
        super(message);
    }
}
