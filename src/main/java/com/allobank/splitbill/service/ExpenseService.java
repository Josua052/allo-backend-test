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
import com.allobank.splitbill.repository.ExpenseRepository;
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

    @Transactional
    public ExpenseResponseDto addExpense(UUID groupId, ExpenseRequestDto request) {
        Group group = groupService.getGroupById(groupId);

        Map<UUID, Participant> participantMap = group.getParticipants().stream()
                .collect(Collectors.toMap(Participant::getId, p -> p));

        Participant paidBy = getParticipantSafely(participantMap, request.paidByParticipantId());

        BigDecimal calculatedTotal = request.splits().stream()
                .map(splitDto -> splitDto.amountOwed())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.amount().compareTo(calculatedTotal) != 0) {
            throw new AmountMismatchException("Total amount does not match the sum of splits");
        }

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setAmount(request.amount());
        expense.setDescription(request.description());

        List<ExpenseSplit> splits = request.splits().stream().map(splitDto -> {
            Participant participant = getParticipantSafely(participantMap, splitDto.participantId());
            ExpenseSplit split = new ExpenseSplit();
            split.setParticipant(participant);
            split.setAmountOwed(splitDto.amountOwed());
            split.setExpense(expense);
            return split;
        }).collect(Collectors.toList());

        expense.setSplits(splits);
        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponseDto(savedExpense);
    }

    // Helper method to retrieve a participant from the map or throw a custom exception
    private Participant getParticipantSafely(Map<UUID, Participant> map, UUID participantId) {
        Participant participant = map.get(participantId);
        if (participant == null) {
            throw new InvalidParticipantException("Participant " + participantId + " does not belong to this group");
        }
        return participant;
    }

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
