package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.ApiResponseDto;
import com.allobank.splitbill.dto.ExpenseRequestDto;
import com.allobank.splitbill.dto.ExpenseResponseDto;
import com.allobank.splitbill.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// REST endpoints for adding expenses to a specific group
@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // Records a new expense after passing JSON payloads through strict JSR-380 validation
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<ExpenseResponseDto> addExpense(
            @PathVariable UUID groupId, 
            @RequestBody @Valid ExpenseRequestDto request) {
        
        ExpenseResponseDto response = expenseService.addExpense(groupId, request);
        return new ApiResponseDto<>(response);
    }
}
