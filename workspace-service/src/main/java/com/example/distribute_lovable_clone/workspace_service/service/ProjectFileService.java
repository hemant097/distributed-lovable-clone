package com.example.distribute_lovable_clone.workspace_service.service;


import com.example.distributelovableclone.commonlib.dto.FileTreeResponse;

public interface ProjectFileService {
     FileTreeResponse getFileTree(Long projectId);

     String getFileContent(Long projectId, String filePath);

     void saveFile(Long projectId, String filePath, String fileContent);
}
