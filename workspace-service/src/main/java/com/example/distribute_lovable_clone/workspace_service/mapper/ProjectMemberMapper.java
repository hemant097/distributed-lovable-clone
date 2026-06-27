package com.example.distribute_lovable_clone.workspace_service.mapper;

import com.example.distribute_lovable_clone.workspace_service.dto.member.MemberResponse;
import com.example.distribute_lovable_clone.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface ProjectMemberMapper {

    @Mapping(target = "userId" , source = "id")
    @Mapping(target = "role" , constant = "OWNER") //defining constant as user doesn't have role field, also this method is only used for owner
    MemberResponse toProjectMemberResponseFromOwner(User owner);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "role", source = "projectRole")
    MemberResponse toProjectMemberResponseFromMember(ProjectMember member);
}
