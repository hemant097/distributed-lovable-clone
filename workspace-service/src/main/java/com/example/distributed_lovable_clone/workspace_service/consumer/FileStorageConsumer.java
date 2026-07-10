package com.example.distributed_lovable_clone.workspace_service.consumer;

import com.example.distributed_lovable_clone.common_lib.event.FileStoreRequestEvent;
import com.example.distributed_lovable_clone.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {

    private final ProjectFileService projectFileService;

    @KafkaListener(topics = "file-storage-request-event",groupId = "workspace-group")
    public void consumeFileEvent(FileStoreRequestEvent requestEvent ){
        log.info("Saving file: {}",requestEvent.filePath());
        projectFileService.saveFile(requestEvent.projectId(),requestEvent.filePath(),requestEvent.fileContent());
    }
}
