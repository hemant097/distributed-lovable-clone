package com.example.distributed_lovable_clone.workspace_service.service.impl;

import com.example.distributed_lovable_clone.workspace_service.entity.Project;
import com.example.distributed_lovable_clone.workspace_service.entity.ProjectFile;
import com.example.distributed_lovable_clone.workspace_service.mapper.ProjectFileMapper;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectFileRepository;
import com.example.distributed_lovable_clone.workspace_service.repository.ProjectRepository;
import com.example.distributed_lovable_clone.workspace_service.service.ProjectFileService;
import com.example.distributed_lovable_clone.common_lib.dto.FileNode;
import com.example.distributed_lovable_clone.common_lib.dto.FileTreeResponse;
import com.example.distributed_lovable_clone.common_lib.errors.ResourceNotFoundException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectRepository projectRepo;
    private final ProjectFileRepository projectFileRepo;
    private final MinioClient minioClient;
    private final ProjectFileMapper projectFileMapper;

    @Value("${minio.project-bucket}")
    private String PROJECTS_BUCKET;

    @Override
    public FileTreeResponse getFileTree(Long projectId) {
        List<ProjectFile> projectFileList = projectFileRepo.findByProjectId(projectId);
        List<FileNode> projectFileNodes =  projectFileMapper.toListOfFileNode(projectFileList);
        return new FileTreeResponse(projectFileNodes);
    }

    @Override
    public String getFileContent(Long projectId, String filePath) {
        String objectKey = "project-"+projectId + "/" + filePath;
        log.info("Trying to get file content from minio at {}",objectKey);
        try (
                InputStream is = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(PROJECTS_BUCKET)
                                .object(objectKey )
                                .build())) {

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to read file: {}/{}", projectId, filePath, e);
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {

        Project project = projectRepo.findById(projectId)
                .orElseThrow( ()-> new ResourceNotFoundException("project",projectId.toString()));

        String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String objectKey = "project-"+projectId +"/"+cleanPath;

        //returns the index of last / OR \ from the cleanPath, if not found,returns -1
        //if index=-1, entire cleanPath is fileName, like in case of cleanPath = "something.txt", fileName = cleanPath
        int index = Math.max(cleanPath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        String fileName = filePath.substring(index + 1);

        log.info("Trying to save file:{}, in projectId:{}",fileName,projectId);

        try {
            byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);

            //saving the file content
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(PROJECTS_BUCKET)
                            .object(objectKey)
                            .stream(inputStream,contentBytes.length, -1)
                            .contentType(determineContentType(cleanPath))
                            .build());

            //saving the metadata
            ProjectFile projectFile = projectFileRepo.findByProjectIdAndPath(projectId,filePath)
                    .orElseGet( () -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .minioObjectKey(objectKey)
                            .createdAt(Instant.now())
                            .build());
            projectFile.setUpdatedAt(Instant.now());
            projectFileRepo.save(projectFile);

            log.info("saved file in minio projects bucket with object_key: {}",objectKey);
        } catch (Exception e){
            log.error("Failed to save file {}/{}",projectId,cleanPath,e);
            throw new RuntimeException("file save failed",e);
        }


    }

    //determines the MIME type by checking the file extension only, e.g. for index.html, it'll return text/html
    String determineContentType(String path){
        String type = URLConnection.guessContentTypeFromName(path);

        if(type!=null) return type;

        if(path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx"))
            return "text/javascript";
        else
            return "text/plain";
    }
}