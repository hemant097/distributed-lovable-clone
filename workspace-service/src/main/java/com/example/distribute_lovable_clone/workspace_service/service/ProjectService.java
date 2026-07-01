package com.example.distribute_lovable_clone.workspace_service.service;


import com.example.distribute_lovable_clone.workspace_service.dto.project.ProjectRequest;
import com.example.distribute_lovable_clone.workspace_service.dto.project.ProjectResponse;
import com.example.distribute_lovable_clone.workspace_service.dto.project.ProjectSummaryResponse;
import com.example.distributelovableclone.commonlib.enums.ProjectPermission;

import java.util.List;

public interface ProjectService {
     List<ProjectSummaryResponse> getUserProjects();

     ProjectSummaryResponse getUserProjectById(Long projectId);

     ProjectResponse createProject(ProjectRequest request);

     ProjectResponse updateProject(Long projectId, ProjectRequest request);

     void softDelete(Long id);

     boolean hasPermission(Long projectId, ProjectPermission permission);
}
