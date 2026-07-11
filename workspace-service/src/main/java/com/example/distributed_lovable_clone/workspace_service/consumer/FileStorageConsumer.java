package com.example.distributed_lovable_clone.workspace_service.consumer;

import com.example.distributed_lovable_clone.common_lib.event.FileStoreRequestEvent;
import com.example.distributed_lovable_clone.common_lib.event.FileStoreResponseEvent;
import com.example.distributed_lovable_clone.workspace_service.entity.ProcessedEvent;
import com.example.distributed_lovable_clone.workspace_service.repository.ProcessedEventRepository;
import com.example.distributed_lovable_clone.workspace_service.service.ProjectFileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {

    private final ProjectFileService projectFileService;
    private final ProcessedEventRepository processedEventRepo;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "file-storage-request-event",groupId = "workspace-group")
    public void consumeFileEvent(FileStoreRequestEvent requestEvent ){

        // checking for idempotency
        if(processedEventRepo.existsById(requestEvent.sagaId())){
            log.info("Duplicate saga detected: {}, resending previous ACK.",requestEvent.sagaId());
            sendResponse(requestEvent,true,null);
            return;
        }

        try {
            log.info("Saving file: {}", requestEvent.filePath());
            projectFileService.saveFile(requestEvent.projectId(), requestEvent.filePath(), requestEvent.fileContent());
            processedEventRepo.save(new ProcessedEvent(requestEvent.sagaId(), LocalDateTime.now()));
            sendResponse(requestEvent,true,null);
        }catch (Exception e){
            log.error("Error in saving file: {}",e.getMessage());
            sendResponse(requestEvent,false,e.getMessage());
        }
    }

    private void sendResponse(FileStoreRequestEvent requestEvent, boolean success, String error){
        FileStoreResponseEvent responseEvent = new FileStoreResponseEvent(
                requestEvent.sagaId(),
                success,
                error,
                requestEvent.projectId()
        );
        kafkaTemplate.send("file-store-responses",responseEvent);
    }
}
