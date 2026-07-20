package com.example.distributed_lovable_clone.intelligence_service.security;

import com.example.distributed_lovable_clone.intelligence_service.client.WorkspaceClient;
import com.example.distributed_lovable_clone.common_lib.enums.ProjectPermission;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;


@Component(value = "security")
@RequiredArgsConstructor
@Slf4j
public class SecurityExpressions {
    private final WorkspaceClient workspaceClient;

    private boolean hasPermission(Long projectId, ProjectPermission permission) {
        try{
            return workspaceClient.checkPermission(projectId, permission);
        }catch (FeignException.Unauthorized ex) {
            log.warn("Token expired or invalid during permission check for project: {}", projectId);
            throw new CredentialsExpiredException("JWT token is expired or invalid");
        }catch (FeignException fe){
            log.error("Workspace service failed during permission check:{}", fe.getMessage());
            return false;
        }
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
