package com.example.distributed_lovable_clone.workspace_service.service;


import com.example.distributed_lovable_clone.workspace_service.dto.project.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);
}
