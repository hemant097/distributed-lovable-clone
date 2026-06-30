package com.example.distribute_lovable_clone.intelligence_service.service.impl;

import com.example.distribute_lovable_clone.intelligence_service.dto.chat.ChatResponse;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatMessage;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatSession;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatSessionId;
import com.example.distribute_lovable_clone.intelligence_service.mapper.ChatMapper;
import com.example.distribute_lovable_clone.intelligence_service.repository.ChatMessageRepository;
import com.example.distribute_lovable_clone.intelligence_service.repository.ChatSessionRepository;
import com.example.distribute_lovable_clone.intelligence_service.service.ChatService;
import com.example.distributelovableclone.commonlib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepo;
    private final ChatSessionRepository chatSessionRepo;
    private final ChatMapper chatMapper;
    private final AuthUtil authUtil;


    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        ChatSession chatSession = chatSessionRepo.getReferenceById(
                new ChatSessionId(projectId,userId)
        );

        List<ChatMessage> chatMessageList = chatMessageRepo.findByChatSession(chatSession);
        return chatMapper.fromListOfChatMessage(chatMessageList);
    }
}
