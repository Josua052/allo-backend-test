package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

// Repository for Participant entity operations
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
}
