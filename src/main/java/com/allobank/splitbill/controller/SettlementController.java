package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.ApiResponseDto;
import com.allobank.splitbill.dto.SettlementResponseDto;
import com.allobank.splitbill.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// REST endpoints for retrieving calculated debt settlements
@RestController
@RequestMapping("/api/v1/groups/{groupId}/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // Executes the core settlement algorithm and returns the minimum transfer instructions
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseDto<SettlementResponseDto> getSettlement(@PathVariable UUID groupId) {
        SettlementResponseDto response = settlementService.calculateSettlement(groupId);
        return new ApiResponseDto<>(response);
    }
}
