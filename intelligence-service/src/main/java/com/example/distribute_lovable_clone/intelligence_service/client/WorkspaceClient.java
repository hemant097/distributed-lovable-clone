package com.example.distribute_lovable_clone.intelligence_service.client;

import com.example.distributelovableclone.commonlib.dto.FileTreeResponse;
import com.example.distributelovableclone.commonlib.enums.ProjectPermission;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "workspace-service",path = "/workspace")
public interface WorkspaceClient {

    @GetMapping("/internal/v1/projects/{projectId}/files/tree")
    FileTreeResponse getFileTree(@PathVariable Long projectId,
                                 @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/internal/v1/projects/{projectId}/files/content")
    String getFileContent(@PathVariable Long projectId,
                          @RequestParam(name = "path") String filePath,
                          @RequestHeader("Authorization") String authorizationHeader
    );

    @GetMapping("/internal/v1/projects/{projectId}/permissions/check")
    boolean checkPermission(@PathVariable Long projectId,
                            @RequestParam ProjectPermission permission);

}
