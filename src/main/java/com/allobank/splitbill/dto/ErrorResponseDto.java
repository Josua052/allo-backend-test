package com.allobank.splitbill.dto;

import java.time.LocalDateTime;

// Envelope for structured error responses to provide clear debugging context
public record ErrorResponseDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}
