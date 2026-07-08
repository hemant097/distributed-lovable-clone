package com.example.distributed_lovable_clone.intelligence_service.dto.chat;


import com.example.distributed_lovable_clone.commonlib.enums.MessageRole;

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
