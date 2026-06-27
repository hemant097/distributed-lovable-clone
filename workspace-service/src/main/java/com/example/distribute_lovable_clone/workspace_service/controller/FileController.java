package com.example.distribute_lovable_clone.workspace_service.controller;


import com.example.distribute_lovable_clone.workspace_service.dto.project.FileContentResponse;
import com.example.distribute_lovable_clone.workspace_service.dto.project.FileTreeResponse;
import com.example.distribute_lovable_clone.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/files")
public class FileController {

    private final ProjectFileService projectFileService;

    @GetMapping
    public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId){
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("/content") //filePath e.g., project-9/src/pages/Index.tsx
    public ResponseEntity<FileContentResponse> getFile(@PathVariable Long projectId,
                                                       @RequestParam(name = "path") String filePath){
        return ResponseEntity.ok(projectFileService.getFileContent(projectId, filePath));
    }


}
