package com.example.distribute_lovable_clone.intelligence_service.dto.chat;


import com.example.distributelovableclone.commonlib.enums.ChatEventType;

public record ChatEventResponse(
        Long id,
        ChatEventType type,
        Integer sequenceOrder,
        String content,
        String filePath,
        String metadata) {
}
