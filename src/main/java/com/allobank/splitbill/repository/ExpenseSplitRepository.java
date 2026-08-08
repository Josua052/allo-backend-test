package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

// Repository for ExpenseSplit entity operations
@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {
}
