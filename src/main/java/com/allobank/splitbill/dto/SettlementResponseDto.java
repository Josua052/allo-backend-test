package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// DTO representing the final calculated settlement for a group
public record SettlementResponseDto(
        UUID groupId,
        List<TransactionDto> transactions,
        int serviceChargePct,
        BigDecimal serviceChargeAmount
) {}
