package com.example.distribute_lovable_clone.common_lib.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.example.distribute_lovable_clone.common_lib.enums.ProjectPermission.*;


@RequiredArgsConstructor
@Getter
public enum ProjectRole {
    EDITOR(Set.of(VIEW, EDIT, DELETE, VIEW_MEMBERS)),
    VIEWER(Set.of(VIEW, VIEW_MEMBERS)),
    OWNER(VIEW, EDIT, DELETE, MANAGE_MEMBERS, VIEW_MEMBERS);

    //using varargs
    ProjectRole(ProjectPermission... permissions){
        this.permissions = Set.of(permissions);
    }

    private final Set<ProjectPermission> permissions;
}
