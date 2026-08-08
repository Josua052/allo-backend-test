package com.allobank.splitbill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// DTO representing the main expense request with cascaded validation
public record ExpenseRequestDto(
        @NotNull(message = "Paid by participant ID cannot be null")
        UUID paidByParticipantId,

        @NotNull(message = "Total amount cannot be null")
        @Positive(message = "Total amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Description cannot be blank")
        String description,

        @NotNull(message = "Split strategy cannot be null")
        com.allobank.splitbill.model.SplitStrategy splitStrategy,

        @NotEmpty(message = "Splits cannot be empty")
        @Valid
        List<ExpenseSplitRequestDto> splits
) {}
