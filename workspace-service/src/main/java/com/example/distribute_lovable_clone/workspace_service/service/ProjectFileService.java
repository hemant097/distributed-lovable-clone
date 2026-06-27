package com.example.distribute_lovable_clone.workspace_service.service;

import com.example.distribute_lovable_clone.workspace_service.dto.project.FileContentResponse;
import com.example.distribute_lovable_clone.workspace_service.dto.project.FileTreeResponse;

public interface ProjectFileService {
     FileTreeResponse getFileTree(Long projectId);

     FileContentResponse getFileContent(Long projectId, String filePath);

     void saveFile(Long projectId, String filePath, String fileContent);
}
