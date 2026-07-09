package com.example.distributed_lovable_clone.workspace_service.service;


import com.example.distributed_lovable_clone.common_lib.dto.FileTreeResponse;

public interface ProjectFileService {
     FileTreeResponse getFileTree(Long projectId);

     String getFileContent(Long projectId, String filePath);

     void saveFile(Long projectId, String filePath, String fileContent);
}
