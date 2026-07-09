package com.example.distributed_lovable_clone.workspace_service.entity;


import com.example.distributed_lovable_clone.common_lib.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
@Entity @Table(name = "project_members")
public class ProjectMember {

    @EmbeddedId
    ProjectMemberId id;

    @ManyToOne
    @MapsId("projectId") //refers to the projectId of the ProjectMemberId
    Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProjectRole projectRole;

    Instant invitedAt;
    Instant acceptedAt;
}
