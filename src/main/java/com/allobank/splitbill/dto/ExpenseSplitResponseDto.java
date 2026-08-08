package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.util.UUID;

// DTO for returning individual split details to the client
public record ExpenseSplitResponseDto(
        UUID id,
        UUID participantId,
        String participantName,
        BigDecimal amountOwed
) {}
