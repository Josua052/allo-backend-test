package com.allobank.splitbill.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

// Represents the individual owed portion of a specific expense
@Entity
@Table(name = "expense_splits")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    // Strict requirement: using BigDecimal for monetary values to prevent precision loss
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amountOwed;
}
