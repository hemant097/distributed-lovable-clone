package com.example.distribute_lovable_clone.workspace_service.controller;

import com.example.distribute_lovable_clone.workspace_service.service.ProjectFileService;
import com.example.distribute_lovable_clone.workspace_service.service.ProjectService;
import com.example.distributelovableclone.commonlib.dto.FileTreeResponse;
import com.example.distributelovableclone.commonlib.enums.ProjectPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class InternalWorkspaceController {

    private final ProjectService projectService;
    private final ProjectFileService projectFileService;

    @GetMapping("/projects/{projectId}/files/tree")
    public FileTreeResponse getFileTree(@PathVariable Long projectId){
        return projectFileService.getFileTree(projectId);
    }

    @GetMapping("/projects/{projectId}/files/content")
    public String getFileContent(@PathVariable Long projectId, @RequestParam String path){
        return projectFileService.getFileContent(projectId,path);
    }

    @GetMapping("/projects/{projectId}/permissions/check")
    public boolean checkProjectPermission(@PathVariable Long projectId,
                                          @RequestParam ProjectPermission permission){
        return projectService.hasPermission(projectId,permission);
    }
}
