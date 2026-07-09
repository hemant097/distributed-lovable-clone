package com.example.distributed_lovable_clone.workspace_service.repository;

import com.example.distributed_lovable_clone.workspace_service.entity.Project;
import com.example.distributed_lovable_clone.common_lib.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            SELECT p as project, pm.projectRole as projectRole
            FROM Project p
            JOIN ProjectMember pm on pm.project.id = p.id
            WHERE p.deletedAt IS NULL
                AND pm.id.userId = :userId
            """)
    List<ProjectWithRole> findAllAccessibleByUser(@Param("userId") Long userId);

    @Query("""
            SELECT p FROM Project p
            WHERE p.deletedAt IS NULL
                AND p.id = :projectId
                AND EXISTS (
                        SELECT 1 FROM ProjectMember pm
                        WHERE (pm.id.projectId,pm.id.userId)=(p.id,:userId)
                        )

        """)
    Optional<Project> findAccessibleProjectById(@Param("projectId") Long projectId,
                                                @Param("userId") Long userId);


    @Query("""
            SELECT p as project, pm.projectRole as projectRole
            FROM Project p
            JOIN ProjectMember pm on pm.project.id = p.id
            WHERE p.deletedAt IS NULL
                AND p.id = :projectId
                AND pm.id.userId = :userId

        """)
    Optional<ProjectWithRole> findAccessibleProjectByIdWithRole(@Param("projectId") Long projectId,
                                                @Param("userId") Long userId);


    interface ProjectWithRole{
        Project getProject();
        ProjectRole getProjectRole();
    }
}
