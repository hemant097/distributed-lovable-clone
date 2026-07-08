package com.example.distributed_lovable_clone.workspace_service.repository;

import com.example.distributed_lovable_clone.workspace_service.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {

    Optional<ProjectFile> findByProjectIdAndPath(Long projectId,String path);

    List<ProjectFile> findByProjectId(Long projectId);
}
