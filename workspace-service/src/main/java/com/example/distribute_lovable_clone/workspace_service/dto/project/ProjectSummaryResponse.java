package com.example.distribute_lovable_clone.workspace_service.dto.project;

import com.example.distributelovableclone.commonlib.enums.ProjectRole;

import java.time.Instant;

public record ProjectSummaryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        ProjectRole role
) {
}
