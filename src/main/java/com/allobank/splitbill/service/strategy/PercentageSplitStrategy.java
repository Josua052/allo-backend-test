package com.allobank.splitbill.service.strategy;

import com.allobank.splitbill.dto.ExpenseRequestDto;
import com.allobank.splitbill.dto.ExpenseSplitRequestDto;
import com.allobank.splitbill.exception.AmountMismatchException;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseSplit;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.model.SplitStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Percentage Strategy implementation solving the Penny Problem
@Component
public class PercentageSplitStrategy implements SplitCalculationStrategy {

    @Override
    public SplitStrategy getStrategyType() {
        return SplitStrategy.PERCENTAGE;
    }

    @Override
    public void validate(ExpenseRequestDto request) {
        BigDecimal calculatedTotal = request.splits().stream()
                .map(ExpenseSplitRequestDto::amountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (new BigDecimal("100").compareTo(calculatedTotal) != 0) {
            throw new AmountMismatchException("Total percentage must exactly equal 100");
        }
    }

    @Override
    public List<ExpenseSplit> calculateSplits(ExpenseRequestDto request, Map<UUID, Participant> participantMap, Expense expense) {
        List<ExpenseSplit> splits = new ArrayList<>();
        BigDecimal totalAmount = request.amount();
        BigDecimal currentSum = BigDecimal.ZERO;
        
        List<ExpenseSplitRequestDto> splitDtos = request.splits();
        
        for (int i = 0; i < splitDtos.size(); i++) {
            ExpenseSplitRequestDto splitDto = splitDtos.get(i);
            Participant participant = SplitCalculationStrategy.getParticipantSafely(participantMap, splitDto.participantId());
            
            ExpenseSplit split = new ExpenseSplit();
            split.setParticipant(participant);
            split.setExpense(expense);
            
            if (i == splitDtos.size() - 1) {
                // PENNY PROBLEM SOLUTION: Last person gets the exact remainder
                BigDecimal remainder = totalAmount.subtract(currentSum);
                split.setAmountOwed(remainder);
            } else {
                // (totalAmount * percentage) / 100
                BigDecimal calculatedAmount = totalAmount.multiply(splitDto.amountOwed())
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                split.setAmountOwed(calculatedAmount);
                currentSum = currentSum.add(calculatedAmount);
            }
            
            splits.add(split);
        }
        
        return splits;
    }
}
