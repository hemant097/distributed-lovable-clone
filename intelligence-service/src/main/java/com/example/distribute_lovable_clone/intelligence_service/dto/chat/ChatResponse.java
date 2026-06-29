package com.example.distribute_lovable_clone.intelligence_service.dto.chat;


import com.example.distributelovableclone.commonlib.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,
        MessageRole role,
        List<ChatEventResponse> events,
        String content,
        Integer tokensUsed,
        Instant createdAt

) {
}
