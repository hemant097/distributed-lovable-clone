package com.example.distributed_lovable_clone.workspace_service.dto.member;

import com.example.distributed_lovable_clone.commonlib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(

        @NotNull
        ProjectRole role
) {
}
