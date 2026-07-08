package com.example.distributed_lovable_clone.workspace_service.repository;

import com.example.distributed_lovable_clone.workspace_service.entity.ProjectMember;
import com.example.distributed_lovable_clone.workspace_service.entity.ProjectMemberId;
import com.example.distributed_lovable_clone.commonlib.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    //using the projectId part of the composite key
    List<ProjectMember> findByIdProjectId(Long projectId);

    //return the project_role where project_id and user_id match
    @Query("""
            SELECT pm.projectRole from ProjectMember pm
            WHERE pm.id.projectId =:projectId AND pm.id.userId=:userId
        """)
    Optional<ProjectRole> getProjectRoleByProjectIdAndUserId(@Param("projectId") Long projectId,
                                                             @Param("userId") Long userId);

    @Query("""
    SELECT COUNT(pm) from ProjectMember pm WHERE pm.id.userId=:userId AND
        pm.projectRole = 'OWNER'
    """)
    int countProjectsOwnedByUser(@Param("userId") Long userId);
}
