package com.allobank.splitbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

// DTO for accepting group creation payloads
public record GroupRequestDto(
        @NotBlank(message = "Group name cannot be blank")
        String name,

        @NotEmpty(message = "Group must have at least one participant")
        List<@NotBlank(message = "Participant name cannot be blank") String> participantNames
) {}
