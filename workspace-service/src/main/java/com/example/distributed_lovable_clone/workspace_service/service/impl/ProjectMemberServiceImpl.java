package com.example.distributed_lovable_clone.workspace_service.service.impl;

import com.example.distributed_lovable_clone.workspace_service.client.AccountClient;
import com.example.distributed_lovable_clone.workspace_service.dto.member.ApproveInviteRequest;
import com.example.distributed_lovable_clone.workspace_service.dto.member.InviteMemberRequest;
import com.example.distributed_lovable_clone.workspace_service.dto.member.MemberResponse;
import com.example.distributed_lovable_clone.workspace_service.dto.member.UpdateMemberRoleRequest;
import com.example.distributed_lovable_clone.workspace_service.entity.Project;
import com.example.distributed_lovable_clone.workspace_service.entity.ProjectMember;
import com.example.distributed_lovable_clone.workspace_service.entity.ProjectMemberId;
import com.example.distributed_lovable_clone.workspace_service.mapper.ProjectMemberMapper;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectMemberRepository;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectRepository;
import com.example.distributed_lovable_clone.workspace_service.service.ProjectMemberService;
import com.example.distributed_lovable_clone.common_lib.dto.UserDto;
import com.example.distributed_lovable_clone.common_lib.errors.ResourceNotFoundException;
import com.example.distributed_lovable_clone.common_lib.security.AuthUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
@Slf4j
public class ProjectMemberServiceImpl implements ProjectMemberService {

    ProjectMemberRepository projectMemberRepo;
    ProjectRepository projectRepo;
    ProjectMemberMapper projectMemberMapper;
    AuthUtil authUtil;
    AccountClient accountClient;


    @Override
    @PreAuthorize("@security.canViewMembers(#projectId)")
    public List<MemberResponse> getProjectMembers(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        checkIfProjectPresent(projectId, userId);

        return projectMemberRepo.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toProjectMemberResponseFromMember)
                .toList();
    }

    @Override
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = checkIfProjectPresent(projectId, userId);
        project.setIsPublic(true); //can invite to a public project only
        projectRepo.save(project);

//        //no access to invite
//        if(!project.getOwner().getId().equals(userId))
//            throw new RuntimeException("Not allowed");

        UserDto invitee = accountClient.getUserByEmail(request.username())
                .orElseThrow(()-> new ResourceNotFoundException("user",request.username()));
        if(invitee.id().equals(userId))
            throw new RuntimeException("Cannot invite yourself");

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,invitee.id());

        if(projectMemberRepo.existsById(projectMemberId)) {
            log.info("project member id, userID {}, projectId {}",userId, projectId);
            throw new RuntimeException("Cannot invite again, already a member");
        }

        ProjectMember member = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
//                .user(invitee)
                .projectRole(request.role())
                .invitedAt(Instant.now())
                .build();

        projectMemberRepo.save(member);

        return projectMemberMapper.toProjectMemberResponseFromMember(member);

    }

    @Override
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request) {
        Long userId = authUtil.getCurrentUserId();

        checkIfProjectPresent(projectId, userId);

//        if(!project.getOwner().getId().equals(userId))
//            throw new RuntimeException("Not allowed");

        ProjectMember projectMember = projectMemberRepo.findById(new ProjectMemberId(projectId,memberId))
                .orElseThrow(()-> new ResourceNotFoundException("member ",memberId.toString()));

        projectMember.setProjectRole(request.role());
        projectMemberRepo.save(projectMember);

        return projectMemberMapper.toProjectMemberResponseFromMember(projectMember);

    }

    @Override
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public void removeProjectMember(Long projectId, Long memberId) {
        Long userId = authUtil.getCurrentUserId();
        checkIfProjectPresent(projectId, userId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);
        boolean exists = projectMemberRepo.existsById(projectMemberId);

        if(!exists)
            throw new RuntimeException("Member not found in project");
        else
            projectMemberRepo.deleteById(projectMemberId);

    }

    @Override
    public boolean approveInvite(Long projectId, ApproveInviteRequest request, Long inviteeId) {
//        //assuming if a user has received an invitation, project MUST exist before
//
//        UserDto invitee = userRepo.findById(inviteeId)
//                .orElseThrow( () -> new ResourceNotFoundException("User",inviteeId.toString()));
//
//        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,invitee.getId());
//        ProjectMember invitedMember = projectMemberRepo
//                .findById(projectMemberId)
//                .orElseThrow(() -> new ResourceNotFoundException("Invite",projectId.toString()));
//
//        if(request.decision().equals("YES")){
//            invitedMember.setAcceptedAt(Instant.now());
//            projectMemberRepo.save(invitedMember);
//            log.info("Request accepted by user :{}, for project id: {}",invitee.getUsername(),projectId);
//            return true;
//        }
//        else {//request rejected
//            projectMemberRepo.deleteById(projectMemberId);
//            log.info("Request rejected by user :{}, for project id: {}",invitee.getUsername(),projectId);
//            return false;
//        }
        return false;
    }


    //Internal functions
    Project checkIfProjectPresent(Long projectId, Long userId){
        return projectRepo.findAccessibleProjectById(projectId, userId)
                .orElseThrow( () -> new ResourceNotFoundException("Project",projectId.toString()));
    }
}
