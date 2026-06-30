package com.example.distribute_lovable_clone.intelligence_service.entity;

import com.example.distributelovableclone.commonlib.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "chat_messages")
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "project_id", referencedColumnName = "projectId",nullable = false),
            @JoinColumn(name = "user_id",referencedColumnName = "userId",nullable = false)
    })
    ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MessageRole role; //USER, ASSISTANT, SYSTEM, TOOL

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    List<ChatEvent> events; //empty unless MessageRole is ASSISTANT

    @Column(columnDefinition = "text")
    String content; //null unless USER role

    String toolCalls; // JSON array of tools called

    @Builder.Default
    Integer tokensUsed = 0;

    @CreationTimestamp
    Instant createdAt;

}
