package com.example.distributed_lovable_clone.workspace_service.controller;


import com.example.distributed_lovable_clone.workspace_service.service.ProjectFileService;
import com.example.distributed_lovable_clone.commonlib.dto.FileTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class FileController {

    private final ProjectFileService projectFileService;

    @GetMapping("/tree")
    public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId){
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("/content") //filePath e.g., project-9/src/pages/Index.tsx
    public ResponseEntity<String> getFile(@PathVariable Long projectId,
                                                       @RequestParam(name = "path") String filePath){
        return ResponseEntity.ok(projectFileService.getFileContent(projectId, filePath));
    }


}
