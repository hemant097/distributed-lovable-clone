package com.example.distributed_lovable_clone.workspace_service.mapper;

import com.example.distributed_lovable_clone.workspace_service.dto.project.ProjectResponse;
import com.example.distributed_lovable_clone.workspace_service.dto.project.ProjectSummaryResponse;
import com.example.distributed_lovable_clone.workspace_service.entity.Project;
import com.example.distributed_lovable_clone.common_lib.enums.ProjectRole;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

//    @Mapping(target = "projectName", source = "name") , if any field names are different
    ProjectSummaryResponse toProjectSummaryResponse(Project project, ProjectRole role);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);
}
