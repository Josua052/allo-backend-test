package com.allobank.splitbill.exception;

// Custom exception thrown when total expense amount does not match the sum of all splits
public class AmountMismatchException extends RuntimeException {
    public AmountMismatchException(String message) {
        super(message);
    }
}
