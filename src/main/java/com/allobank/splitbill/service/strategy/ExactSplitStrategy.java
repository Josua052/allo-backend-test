package com.allobank.splitbill.service.strategy;

import com.allobank.splitbill.dto.ExpenseRequestDto;
import com.allobank.splitbill.exception.AmountMismatchException;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseSplit;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.model.SplitStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// Exact Amount Strategy implementation
@Component
public class ExactSplitStrategy implements SplitCalculationStrategy {

    @Override
    public SplitStrategy getStrategyType() {
        return SplitStrategy.EXACT;
    }

    @Override
    public void validate(ExpenseRequestDto request) {
        BigDecimal calculatedTotal = request.splits().stream()
                .map(splitDto -> splitDto.amountOwed())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.amount().compareTo(calculatedTotal) != 0) {
            throw new AmountMismatchException("Total amount does not match the sum of EXACT splits");
        }
    }

    @Override
    public List<ExpenseSplit> calculateSplits(ExpenseRequestDto request, Map<UUID, Participant> participantMap, Expense expense) {
        return request.splits().stream().map(splitDto -> {
            Participant participant = SplitCalculationStrategy.getParticipantSafely(participantMap, splitDto.participantId());
            ExpenseSplit split = new ExpenseSplit();
            split.setParticipant(participant);
            split.setAmountOwed(splitDto.amountOwed());
            split.setExpense(expense);
            return split;
        }).collect(Collectors.toList());
    }
}
