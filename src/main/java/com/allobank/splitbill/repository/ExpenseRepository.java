package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// Repository for Expense entity operations
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    
    // Finds all expenses associated with a specific group ID
    List<Expense> findByGroupId(UUID groupId);
}
