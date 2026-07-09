package com.example.distributed_lovable_clone.workspace_service.service.impl;

import com.example.distributed_lovable_clone.workspace_service.client.AccountClient;
import com.example.distributed_lovable_clone.workspace_service.dto.project.ProjectRequest;
import com.example.distributed_lovable_clone.workspace_service.dto.project.ProjectResponse;
import com.example.distributed_lovable_clone.workspace_service.dto.project.ProjectSummaryResponse;
import com.example.distributed_lovable_clone.workspace_service.entity.Project;
import com.example.distributed_lovable_clone.workspace_service.entity.ProjectMember;
import com.example.distributed_lovable_clone.workspace_service.entity.ProjectMemberId;
import com.example.distributed_lovable_clone.workspace_service.mapper.ProjectMapper;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectMemberRepository;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectRepository;
import com.example.distributed_lovable_clone.workspace_service.security.SecurityExpressions;
import com.example.distributed_lovable_clone.workspace_service.service.ProjectService;
import com.example.distributed_lovable_clone.workspace_service.service.ProjectTemplateService;
import com.example.distributed_lovable_clone.common_lib.dto.PlanDto;
import com.example.distributed_lovable_clone.common_lib.enums.ProjectPermission;
import com.example.distributed_lovable_clone.common_lib.enums.ProjectRole;
import com.example.distributed_lovable_clone.common_lib.errors.BadRequestException;
import com.example.distributed_lovable_clone.common_lib.errors.ResourceNotFoundException;
import com.example.distributed_lovable_clone.common_lib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepo;
    ProjectMemberRepository projectMemberRepo;
    ProjectMapper projectMapper;
    AuthUtil authUtil;
    ProjectTemplateService projectTemplateService;
    AccountClient accountClient;
    SecurityExpressions securityExpressions;

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        List<ProjectRepository.ProjectWithRole> projectList = projectRepo.findAllAccessibleByUser(userId);

/**       using streams
        //        return projectRepo.findAllAccessibleByUser(userId)
//                .stream()
//                .map(projectMapper::toProjectSummaryResponse)
//                .toList();
 **/

        //        using mapstruct (when findAllAccesible was returning List<Project>)
//        return projectMapper.toListOfProjectSummaryResponse(projectList);

        return projectList.stream()
                .map( pwr -> projectMapper.toProjectSummaryResponse(pwr.getProject(), pwr.getProjectRole()))
                .toList();
    }

    @PreAuthorize("@security.canViewTheProject(#projectId)")
    @Override
    public ProjectSummaryResponse getUserProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        ProjectRepository.ProjectWithRole projectWRole = projectRepo.findAccessibleProjectByIdWithRole(projectId,userId)
                .orElseThrow(()-> new BadRequestException("Project not found"));
        return projectMapper.toProjectSummaryResponse(projectWRole.getProject(),projectWRole.getProjectRole() );
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        if(!canCreateProject()) {
            throw new BadRequestException("User cannot create any more projects with the current plan. Upgrade for more privileges.");
        }

        Long projectOwnerUserId = authUtil.getCurrentUserId();
        log.info("Trying to create new project for userId:{}, with name:{}",projectOwnerUserId, request.name());

        Project newProject = Project.builder()
                .name(request.name())
                .build();

        newProject = projectRepo.save(newProject);

        ProjectMemberId projectMemberId = new ProjectMemberId(newProject.getId(), projectOwnerUserId);
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .project(newProject)
                .projectRole(ProjectRole.OWNER)
//                .user(owner)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .build();
        projectMemberRepo.save(projectMember);
        log.info("New project created, with id:{}, and name:{}",newProject.getId(), request.name());

        //initializing project with template project
        projectTemplateService.initializeProjectFromTemplate(newProject.getId());

        return projectMapper.toProjectResponse(newProject);

    }


    @Override
    @PreAuthorize("@security.canEditTheProject(#projectId)")
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = checkIfProjectPresent(projectId,userId);

//        if(!project.getOwner().getId().equals(userId)){
//            throw new RuntimeException("You are not allowed to update the name");
//        }

        project.setName(request.name());
        project = projectRepo.save(project);

        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteTheProject(#projectId)")
    public void softDelete(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = checkIfProjectPresent(projectId,userId);

//        if(!project.getOwner().getId().equals(userId)){
//            throw new RuntimeException("You are not allowed to delete this project");
//        }

        project.setDeletedAt(Instant.now());
        projectRepo.save(project);


    }

    @Override
    public boolean hasPermission(Long projectId, ProjectPermission permission) {
        return securityExpressions.hasPermission(projectId,permission);
    }

    //Internal functions
    Project checkIfProjectPresent(Long projectId, Long userId){
        return projectRepo.findAccessibleProjectById(projectId, userId)
                .orElseThrow( () -> new ResourceNotFoundException("Project",projectId.toString()));
    }

    private boolean canCreateProject(){
        Long userId = authUtil.getCurrentUserId();
        if(userId==null) return false;

        PlanDto plan = accountClient.getCurrentSubscribedPlanByUser();

        int maxAllowed = plan.maxProjects();
        int ownedCount = projectMemberRepo.countProjectsOwnedByUser(userId);

        return ownedCount < maxAllowed;


    }


}
