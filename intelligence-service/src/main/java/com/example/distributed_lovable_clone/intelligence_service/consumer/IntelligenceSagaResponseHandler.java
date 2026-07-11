package com.example.distributed_lovable_clone.intelligence_service.consumer;

import com.example.distributed_lovable_clone.common_lib.enums.ChatEventStatus;
import com.example.distributed_lovable_clone.common_lib.event.FileStoreResponseEvent;
import com.example.distributed_lovable_clone.intelligence_service.entity.ChatEvent;
import com.example.distributed_lovable_clone.intelligence_service.repository.ChatEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntelligenceSagaResponseHandler {

    private final ChatEventRepository chatEventRepo;

    @Transactional
    @KafkaListener(topics = "file-store-response", groupId = "intelligence-group")
    public void handleSagaResponse(FileStoreResponseEvent responseEvent) {
        chatEventRepo.findBySagaId(responseEvent.sagaId()).ifPresent(chatEvent -> {

            if (!ChatEventStatus.PENDING.equals(chatEvent.getStatus())) {
                log.info("Response for Saga {} already handled. Skipping.", responseEvent.sagaId());
                return;
            }
            if (responseEvent.success()) {
                chatEvent.setStatus(ChatEventStatus.CONFIRMED);
                log.info("Saga {} CONFIRMED", responseEvent.sagaId());
            } else {
                log.warn("Saga {} FAILED, Deleting event", responseEvent.sagaId());
                chatEvent.setStatus(ChatEventStatus.FAILED);
            }
        });
    }
}
