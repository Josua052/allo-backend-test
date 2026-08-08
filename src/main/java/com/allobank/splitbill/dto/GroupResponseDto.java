package com.allobank.splitbill.dto;

import java.util.List;
import java.util.UUID;

// DTO for returning complete group data securely
public record GroupResponseDto(
        UUID id,
        String name,
        List<ParticipantResponseDto> participants
) {}
