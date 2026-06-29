package com.example.distribute_lovable_clone.intelligence_service.repository;

import com.example.distribute_lovable_clone.intelligence_service.entity.ChatMessage;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {

//to avoid n+1 query problem
    @Query("""
        SELECT DISTINCT cm FROM ChatMessage cm
        LEFT JOIN FETCH cm.events e
        WHERE cm.chatSession = :chatSession
        ORDER BY cm.createdAt ASC,e.sequenceOrder ASC
""")
    List<ChatMessage> findByChatSession(@Param(value = "chatSession") ChatSession chatSession);
}
