package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// DTO for returning the complete expense summary to prevent exposing database entities
public record ExpenseResponseDto(
        UUID id,
        UUID groupId,
        UUID paidByParticipantId,
        String paidByParticipantName,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt,
        List<ExpenseSplitResponseDto> splits
) {}
