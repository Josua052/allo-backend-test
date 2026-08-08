package com.allobank.splitbill.dto;

// Generic envelope for successful API responses to maintain contract stability
public record ApiResponseDto<T>(
        T data
) {}
