package com.example.distribute_lovable_clone.workspace_service.service;


import com.example.distribute_lovable_clone.workspace_service.dto.member.ApproveInviteRequest;
import com.example.distribute_lovable_clone.workspace_service.dto.member.InviteMemberRequest;
import com.example.distribute_lovable_clone.workspace_service.dto.member.MemberResponse;
import com.example.distribute_lovable_clone.workspace_service.dto.member.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService  {
     List<MemberResponse> getProjectMembers(Long projectId);

     MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

     MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

     void removeProjectMember(Long projectId, Long memberId);

     //TODO: add respond to invite functionality
     boolean approveInvite (Long projectId, ApproveInviteRequest request, Long inviteeId);
}
