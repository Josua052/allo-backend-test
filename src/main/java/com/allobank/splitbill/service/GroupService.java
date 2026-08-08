package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.GroupResponseDto;
import com.allobank.splitbill.dto.ParticipantResponseDto;
import com.allobank.splitbill.exception.ResourceNotFoundException;
import com.allobank.splitbill.model.Group;
import com.allobank.splitbill.model.Participant;
import com.allobank.splitbill.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Service layer handling business logic for Group operations
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    // Creates a new group and automatically associates the provided participant names with it
    @Transactional
    public GroupResponseDto createGroup(String name, List<String> participantNames) {
        Group group = new Group();
        group.setName(name);

        List<Participant> participants = participantNames.stream().map(participantName -> {
            Participant participant = new Participant();
            participant.setName(participantName);
            participant.setGroup(group);
            return participant;
        }).collect(Collectors.toList());

        group.setParticipants(participants);
        
        // Cascades save operation to all associated participants automatically
        Group savedGroup = groupRepository.save(group);
        return mapToDto(savedGroup);
    }

    private GroupResponseDto mapToDto(Group group) {
        return new GroupResponseDto(
                group.getId(),
                group.getName(),
                group.getParticipants().stream()
                        .map(p -> new ParticipantResponseDto(p.getId(), p.getName()))
                        .collect(Collectors.toList())
        );
    }

    // Retrieves a group by ID or throws an exception if not found
    @Transactional(readOnly = true)
    public Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
    }
}
