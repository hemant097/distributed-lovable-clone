package com.example.distribute_lovable_clone.workspace_service.dto.member;

import com.example.distributelovableclone.commonlib.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(Long userId,
                             String username,
                             String name,
                             ProjectRole projectRole,
                             Instant invitedAt) {
}

