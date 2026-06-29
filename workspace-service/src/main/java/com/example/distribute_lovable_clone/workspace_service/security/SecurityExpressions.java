package com.example.distribute_lovable_clone.workspace_service.security;

import com.example.distribute_lovable_clone.workspace_service.repository.ProjectMemberRepository;
import com.example.distributelovableclone.commonlib.enums.ProjectPermission;
import com.example.distributelovableclone.commonlib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component(value = "security")
@RequiredArgsConstructor
@Slf4j
public class SecurityExpressions {
    private final ProjectMemberRepository projectMemberRepo;
    private final AuthUtil authUtil;


    private boolean hasPermission(Long projectId, ProjectPermission permission){
        Long userId = authUtil.getCurrentUserId();

        return projectMemberRepo.getProjectRoleByProjectIdAndUserId(projectId, userId)
                .map( role -> {
                    log.info("User id: {}, with project id:{}, has role:{}, with permissions, {}",userId,projectId,role,role.getPermissions());
                    return role.getPermissions() //returns a set of permissions
                            .contains(permission);
                })
                .orElse(false);
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
