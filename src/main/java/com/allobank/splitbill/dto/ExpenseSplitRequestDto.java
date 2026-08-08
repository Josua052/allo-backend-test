package com.allobank.splitbill.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

// DTO representing an individual's owed portion with strict validation
public record ExpenseSplitRequestDto(
        @NotNull(message = "Participant ID cannot be null")
        UUID participantId,

        @NotNull(message = "Amount owed cannot be null")
        @Positive(message = "Amount owed must be greater than zero")
        BigDecimal amountOwed
) {}
