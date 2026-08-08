package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.ApiResponseDto;
import com.allobank.splitbill.dto.GroupRequestDto;
import com.allobank.splitbill.dto.GroupResponseDto;
import com.allobank.splitbill.dto.ParticipantResponseDto;
import com.allobank.splitbill.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// REST endpoints for managing bill groups
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // Creates a new group and wraps the entity result into a standardized response DTO
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<GroupResponseDto> createGroup(@RequestBody @Valid GroupRequestDto request) {
        GroupResponseDto response = groupService.createGroup(request.name(), request.participantNames());
        return new ApiResponseDto<>(response);
    }
}
