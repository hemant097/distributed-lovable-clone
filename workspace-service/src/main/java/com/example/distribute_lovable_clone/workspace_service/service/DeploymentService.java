package com.example.distribute_lovable_clone.workspace_service.service;


import com.example.distribute_lovable_clone.workspace_service.dto.project.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);
}
