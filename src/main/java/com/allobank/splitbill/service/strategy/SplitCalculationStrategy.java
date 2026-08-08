package com.allobank.splitbill.service.strategy;

import com.allobank.splitbill.dto.ExpenseRequestDto;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseSplit;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.model.SplitStrategy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Interface for applying Strategy Design Pattern to split calculations
public interface SplitCalculationStrategy {
    
    SplitStrategy getStrategyType();
    
    // Validates if the input matches the strategy's exact rules
    void validate(ExpenseRequestDto request);
    
    // Calculates the amount owed for each participant
    List<ExpenseSplit> calculateSplits(ExpenseRequestDto request, Map<UUID, Participant> participantMap, Expense expense);

    // Helper method shared across strategies
    static Participant getParticipantSafely(Map<UUID, Participant> map, UUID participantId) {
        Participant participant = map.get(participantId);
        if (participant == null) {
            throw new com.allobank.splitbill.exception.InvalidParticipantException("Participant " + participantId + " does not belong to this group");
        }
        return participant;
    }
}
