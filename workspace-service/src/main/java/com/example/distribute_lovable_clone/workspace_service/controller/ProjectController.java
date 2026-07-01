package com.example.distribute_lovable_clone.workspace_service.controller;

import com.example.distribute_lovable_clone.workspace_service.dto.project.DeployResponse;
import com.example.distribute_lovable_clone.workspace_service.dto.project.ProjectRequest;
import com.example.distribute_lovable_clone.workspace_service.dto.project.ProjectResponse;
import com.example.distribute_lovable_clone.workspace_service.dto.project.ProjectSummaryResponse;
import com.example.distribute_lovable_clone.workspace_service.service.DeploymentService;
import com.example.distribute_lovable_clone.workspace_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final DeploymentService deploymentService;

    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects(){
        return ResponseEntity.ok(projectService.getUserProjects());
    }

    @GetMapping("{id}")
    public ResponseEntity<ProjectSummaryResponse> getProjectById(@PathVariable(name = "id") Long projectId){
        return ResponseEntity.ok(projectService.getUserProjectById(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid ProjectRequest request){
        return new ResponseEntity<>(projectService.createProject(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
                                                         @RequestBody @Valid ProjectRequest request){
        return ResponseEntity.ok(projectService.updateProject(id,request));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id){
        projectService.softDelete(id);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<DeployResponse> deployProject(@PathVariable Long id){
        return ResponseEntity.ok(deploymentService.deploy(id));
    }
}
