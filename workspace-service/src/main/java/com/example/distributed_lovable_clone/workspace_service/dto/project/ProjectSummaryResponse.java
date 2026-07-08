package com.example.distributed_lovable_clone.workspace_service.dto.project;

import com.example.distributed_lovable_clone.commonlib.enums.ProjectRole;

import java.time.Instant;

public record ProjectSummaryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        ProjectRole role
) {
}
