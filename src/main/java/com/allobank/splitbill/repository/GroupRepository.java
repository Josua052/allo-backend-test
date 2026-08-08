package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

// Repository for Group entity operations
@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
}
