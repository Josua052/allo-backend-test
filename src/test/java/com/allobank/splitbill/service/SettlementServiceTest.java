package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.SettlementResponseDto;
import com.allobank.splitbill.dto.TransactionDto;
import com.allobank.splitbill.model.Expense;
import com.allobank.splitbill.model.ExpenseSplit;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit tests for the greedy settlement algorithm and performance benchmarking
@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private SettlementService settlementService;

    private UUID groupId;
    private Participant participantA;
    private Participant participantB;
    private Participant participantC;

    @BeforeEach
    void setUp() {
        // Inject configuration property via reflection
        ReflectionTestUtils.setField(settlementService, "githubUsername", "Josua052");

        groupId = UUID.randomUUID();
        
        participantA = new Participant();
        participantA.setId(UUID.randomUUID());
        participantA.setName("Alice");

        participantB = new Participant();
        participantB.setId(UUID.randomUUID());
        participantB.setName("Bob");

        participantC = new Participant();
        participantC.setId(UUID.randomUUID());
        participantC.setName("Charlie");
    }

    @Test
    void shouldReturnCorrectTransactionsAndServiceCharge_WhenExpensesAreProvided() {
        // Expense 1: Alice paid 300, split equally (100 each)
        Expense expense1 = new Expense();
        expense1.setAmount(new BigDecimal("300"));
        expense1.setPaidBy(participantA);
        expense1.setSplits(List.of(
                createSplit(participantA, "100"),
                createSplit(participantB, "100"),
                createSplit(participantC, "100")
        ));

        // Expense 2: Bob paid 150, split equally (50 each)
        Expense expense2 = new Expense();
        expense2.setAmount(new BigDecimal("150"));
        expense2.setPaidBy(participantB);
        expense2.setSplits(List.of(
                createSplit(participantA, "50"),
                createSplit(participantB, "50"),
                createSplit(participantC, "50")
        ));

        when(expenseRepository.findByGroupId(groupId)).thenReturn(List.of(expense1, expense2));

        SettlementResponseDto response = settlementService.calculateSettlement(groupId);

        // Balances should be:
        // Alice: +300 - 150 = +150 (Creditor)
        // Bob: +150 - 150 = 0 (Neutral)
        // Charlie: 0 - 150 = -150 (Debtor)
        // Transaction: Charlie pays Alice 150
        
        assertEquals(1, response.transactions().size());
        TransactionDto transaction = response.transactions().get(0);
        assertEquals(participantC.getId(), transaction.fromParticipantId());
        assertEquals(participantA.getId(), transaction.toParticipantId());
        assertTrue(new BigDecimal("150").compareTo(transaction.amount()) == 0);
        
        // Josua052 ASCII Sum = 531 % 10 = 1%
        assertEquals(1, response.serviceChargePct());
        // Total expense 450. 1% of 450 = 4.5. Use compareTo to ignore trailing zeroes.
        assertTrue(new BigDecimal("4.5").compareTo(response.serviceChargeAmount()) == 0);

        // Verify mock interactions
        verify(groupService, times(1)).getGroupById(groupId);
    }

    // Helper method to keep test code clean
    private ExpenseSplit createSplit(Participant participant, String amount) {
        ExpenseSplit split = new ExpenseSplit();
        split.setParticipant(participant);
        split.setAmountOwed(new BigDecimal(amount));
        return split;
    }
}
