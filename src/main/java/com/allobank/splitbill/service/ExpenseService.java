package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.ExpenseRequestDto;
import com.allobank.splitbill.dto.ExpenseResponseDto;
import com.allobank.splitbill.dto.ExpenseSplitResponseDto;
import com.allobank.splitbill.exception.AmountMismatchException;
import com.allobank.splitbill.exception.InvalidParticipantException;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseSplit;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.model.SplitStrategy;
import com.allobank.splitbill.repository.ExpenseRepository;
import com.allobank.splitbill.service.strategy.SplitCalculationStrategy;
import com.allobank.splitbill.service.strategy.SplitStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// Service layer handling strict business logic for recording expenses efficiently
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final GroupService groupService;
    private final ExpenseRepository expenseRepository;
    private final SplitStrategyFactory strategyFactory;

    @Transactional
    public ExpenseResponseDto addExpense(UUID groupId, ExpenseRequestDto request) {
        Group group = groupService.getGroupById(groupId);

        Map<UUID, Participant> participantMap = group.getParticipants().stream()
                .collect(Collectors.toMap(Participant::getId, p -> p));

        Participant paidBy = SplitCalculationStrategy.getParticipantSafely(participantMap, request.paidByParticipantId());

        // Get the appropriate strategy and execute
        SplitCalculationStrategy strategy = strategyFactory.getStrategy(request.splitStrategy());
        strategy.validate(request);

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setAmount(request.amount());
        expense.setDescription(request.description());

        List<ExpenseSplit> splits = strategy.calculateSplits(request, participantMap, expense);

        expense.setSplits(splits);
        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponseDto(savedExpense);
    }

    // Helper method getParticipantSafely was moved to SplitCalculationStrategy

    // Maps the saved entity into a clean response DTO to prevent exposing database internals
    private ExpenseResponseDto mapToResponseDto(Expense expense) {
        List<ExpenseSplitResponseDto> splitDtos = expense.getSplits().stream()
                .map(split -> new ExpenseSplitResponseDto(
                        split.getId(),
                        split.getParticipant().getId(),
                        split.getParticipant().getName(),
                        split.getAmountOwed()
                )).collect(Collectors.toList());

        return new ExpenseResponseDto(
                expense.getId(),
                expense.getGroup().getId(),
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getCreatedAt(),
                splitDtos
        );
    }
}
