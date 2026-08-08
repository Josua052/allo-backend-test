package com.allobank.splitbill.dto;

import java.util.UUID;

// DTO for returning participant data without exposing entities
public record ParticipantResponseDto(
        UUID id,
        String name
) {}
