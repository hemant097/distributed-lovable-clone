package com.example.distributed_lovable_clone.workspace_service.service.impl;


import com.example.distributed_lovable_clone.workspace_service.entity.Project;
import com.example.distributed_lovable_clone.workspace_service.entity.ProjectFile;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectFileRepository;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectRepository;
import com.example.distributed_lovable_clone.workspace_service.service.ProjectTemplateService;
import com.example.distributed_lovable_clone.common_lib.errors.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProjectTemplateServiceImpl implements ProjectTemplateService {

    private final MinioClient minioClient;
    private final ProjectFileRepository projectFileRepo;
    private final ProjectRepository projectRepo;

    private static final String TEMPLATE_BUCKET="starter-project";
    private static final String TARGET_BUCKET="projects";
    private static final String TEMPLATE_NAME="react-vite-tailwind-daisyui-starter";

    @Override
    public void initializeProjectFromTemplate(Long projectId) {

        Project project = projectRepo.findById(projectId)
                .orElseThrow(()-> new ResourceNotFoundException("project",projectId.toString()));

        try{
            log.info("Trying to initialize project from template for projectId: {}",projectId);
        // list objects information recursively, from TEMPLATE_BUCKET, whose names start with 'TEMPLATE_NAME/', as
        // object storage is flat, there are no real folders
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(TEMPLATE_BUCKET)
                            .prefix(TEMPLATE_NAME+"/") //as we're getting all the files of this bucket, so optional
                            .recursive(true)
                            .build()
            );
            List<ProjectFile> filesToSave = new ArrayList<>(); //for metadata in postgres db

            //for each file, copy to target project structure, so finally, it gets saved at the destination_key
            //like -> projects/project-1/src/components/file_name.tsx
            for(Result<Item> result : results){
                Item item = result.get();
                String sourceKey = item.objectName();

                String cleanPath = sourceKey.replaceFirst(TEMPLATE_NAME+"/","");
                String destKey = "project-"+projectId+"/"+cleanPath;

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(TARGET_BUCKET)
                                .object(destKey)
                                .source(
                                        CopySource.builder()
                                                .bucket(TEMPLATE_BUCKET)
                                                .object(sourceKey)
                                                .build()
                                )
                                .build()
                );

                ProjectFile pf = ProjectFile.builder()
                        .project(project)
                        .path(cleanPath)
                        .minioObjectKey(destKey)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                filesToSave.add(pf);
            }
            log.info("Project initialized with project template successfully");
            projectFileRepo.saveAll(filesToSave);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize project from template", e);
        }


    }
}
