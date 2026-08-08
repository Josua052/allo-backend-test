package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.SettlementResponseDto;
import com.allobank.splitbill.dto.TransactionDto;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseSplit;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

// Service layer responsible for the core settlement algorithm and optimization
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;

    // Injecting properties rather than hardcoding business rules
    @Value("${app.github.username:GKM605HQ}")
    private String githubUsername;

    // Calculates optimized debts and Allo Bank service charge
    @Transactional(readOnly = true)
    public SettlementResponseDto calculateSettlement(UUID groupId) {
        // Ensure group exists
        groupService.getGroupById(groupId);

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        
        Map<Participant, BigDecimal> balances = new HashMap<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;

        // Calculate Net Balances for everyone
        for (Expense expense : expenses) {
            totalExpenses = totalExpenses.add(expense.getAmount());
            
            // Add to the person who paid
            balances.put(expense.getPaidBy(), balances.getOrDefault(expense.getPaidBy(), BigDecimal.ZERO).add(expense.getAmount()));
            
            // Subtract from the people who owe
            for (ExpenseSplit split : expense.getSplits()) {
                balances.put(split.getParticipant(), balances.getOrDefault(split.getParticipant(), BigDecimal.ZERO).subtract(split.getAmountOwed()));
            }
        }

        List<BalanceNode> debtors = new ArrayList<>();
        List<BalanceNode> creditors = new ArrayList<>();

        for (Map.Entry<Participant, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new BalanceNode(entry.getKey(), balance));
            } else if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new BalanceNode(entry.getKey(), balance));
            }
        }

        debtors.sort((a, b) -> a.amount.compareTo(b.amount)); 
        creditors.sort((a, b) -> b.amount.compareTo(a.amount)); 

        List<TransactionDto> transactions = new ArrayList<>();
        int d = 0;
        int c = 0;

        // Greedy algorithm to minimize transactions
        while (d < debtors.size() && c < creditors.size()) {
            BalanceNode debtor = debtors.get(d);
            BalanceNode creditor = creditors.get(c);

            // Find the minimum absolute amount between debtor and creditor
            BigDecimal amountToTransfer = debtor.amount.abs().min(creditor.amount);

            transactions.add(new TransactionDto(
                    debtor.participant.getId(),
                    debtor.participant.getName(),
                    creditor.participant.getId(),
                    creditor.participant.getName(),
                    amountToTransfer
            ));

            // Adjust balances
            debtor.amount = debtor.amount.add(amountToTransfer);
            creditor.amount = creditor.amount.subtract(amountToTransfer);

            if (debtor.amount.compareTo(BigDecimal.ZERO) == 0) d++;
            if (creditor.amount.compareTo(BigDecimal.ZERO) == 0) c++;
        }

        // Calculate Service Charge
        int serviceChargePct = calculateServiceChargePct(githubUsername);
        
        // Use HALF_UP rounding for financial precision
        BigDecimal serviceChargeAmount = totalExpenses
                .multiply(BigDecimal.valueOf(serviceChargePct))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return new SettlementResponseDto(groupId, transactions, serviceChargePct, serviceChargeAmount);
    }

    // Pure logic method to calculate percentage based on ASCII
    private int calculateServiceChargePct(String username) {
        int sum = 0;
        for (char c : username.toCharArray()) {
            sum += (int) c;
        }
        return sum % 10;
    }

    // Internal helper class to track mutable balances during the algorithm
    private static class BalanceNode {
        Participant participant;
        BigDecimal amount;

        BalanceNode(Participant participant, BigDecimal amount) {
            this.participant = participant;
            this.amount = amount;
        }
    }
}
