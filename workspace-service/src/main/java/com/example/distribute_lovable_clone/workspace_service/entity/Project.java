package com.example.distribute_lovable_clone.workspace_service.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity @Table(name = "projects",
        indexes = { //for faster searches
                @Index(name = "idx_project_updated_at_desc",columnList = "updated_at DESC,deleted_at"),
                @Index(name = "idx_project_deleted_at_updated_at_desc",columnList = "deleted_at,updated_at DESC"),
                @Index(name = "idx_project_deleted_at",columnList = "deleted_at")
        })
public class Project {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Builder.Default
    Boolean isPublic = false; //default

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt; //soft delete

}
