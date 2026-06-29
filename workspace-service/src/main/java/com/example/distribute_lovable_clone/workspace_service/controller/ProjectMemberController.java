package com.example.distribute_lovable_clone.workspace_service.controller;

import com.example.distribute_lovable_clone.workspace_service.dto.member.ApproveInviteRequest;
import com.example.distribute_lovable_clone.workspace_service.dto.member.InviteMemberRequest;
import com.example.distribute_lovable_clone.workspace_service.dto.member.MemberResponse;
import com.example.distribute_lovable_clone.workspace_service.dto.member.UpdateMemberRoleRequest;
import com.example.distribute_lovable_clone.workspace_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getProjectMembers(@PathVariable Long projectId){
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> inviteMember(@PathVariable Long projectId,
                                                       @RequestBody @Valid InviteMemberRequest request){
        return new ResponseEntity<>(projectMemberService.inviteMember(projectId, request), HttpStatus.CREATED);
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateMemberRoleRequest request)
    {
        return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId, memberId, request));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<MemberResponse> removeProject(
            @PathVariable Long projectId,
            @PathVariable Long memberId)
    {
        projectMemberService.removeProjectMember(projectId,memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{memberId}")
    public ResponseEntity<Map<String,String>> approveInvite(@PathVariable Long projectId,
                                                       @PathVariable (name = "memberId") Long inviteeId,
                                                       @RequestBody @Valid ApproveInviteRequest request){

        if(projectMemberService.approveInvite(projectId, request, inviteeId))
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "Invite accepted"
                    )
            );
        else
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "Invite rejected"
                    )
            );
    }
}
