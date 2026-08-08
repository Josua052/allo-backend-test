package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.util.UUID;

// DTO representing a single payment instruction (who pays whom)
public record TransactionDto(
        UUID fromParticipantId,
        String fromParticipantName,
        UUID toParticipantId,
        String toParticipantName,
        BigDecimal amount
) {}
