package com.example.distribute_lovable_clone.workspace_service.dto.member;

import com.example.distributelovableclone.commonlib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(

        @NotNull
        ProjectRole role
) {
}
