package com.example.distribute_lovable_clone.intelligence_service.security;

import com.example.distribute_lovable_clone.intelligence_service.client.WorkspaceClient;
import com.example.distributelovableclone.commonlib.enums.ProjectPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component(value = "security")
@RequiredArgsConstructor
@Slf4j
public class SecurityExpressions {
    private final WorkspaceClient workspaceClient;

    private boolean hasPermission(Long projectId, ProjectPermission permission){
        return workspaceClient.checkPermission(projectId, permission);
    }

    public boolean canViewTheProject(Long projectId){
        return hasPermission(projectId, ProjectPermission.VIEW);
    }

    public boolean canEditTheProject(Long projectId){
        return hasPermission(projectId, ProjectPermission.EDIT);
    }

    public boolean canDeleteTheProject(Long projectId){
        return hasPermission(projectId, ProjectPermission.DELETE);
    }

    public boolean canViewMembers(Long projectId){
        return hasPermission(projectId, ProjectPermission.VIEW_MEMBERS);
    }

    public boolean canManageMembers(Long projectId){
        return hasPermission(projectId, ProjectPermission.MANAGE_MEMBERS);
    }

}
