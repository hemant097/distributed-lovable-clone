package com.example.distributed_lovable_clone.intelligence_service.controller;

import com.example.distributed_lovable_clone.intelligence_service.dto.chat.ChatRequest;
import com.example.distributed_lovable_clone.intelligence_service.dto.chat.ChatResponse;
import com.example.distributed_lovable_clone.intelligence_service.dto.chat.StreamResponse;
import com.example.distributed_lovable_clone.intelligence_service.service.AiGenerationService;
import com.example.distributed_lovable_clone.intelligence_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/chat")
public class ChatController {

    private final AiGenerationService aiGenerationService;
    private final ChatClient chatClient;
    private final ChatService chatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamResponse>> streamChat(
            @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authorizationHeader){
        return aiGenerationService.streamResponse(request.message(), request.projectId(),authorizationHeader)
                .map( data -> ServerSentEvent.<StreamResponse>builder()
                        .data(data)
                        .build());
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(@PathVariable Long projectId){
        return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
    }

    //endpoint to test AI response
    @GetMapping("/test")
    public ResponseEntity<String> poem(@RequestParam String topic,
                                     @RequestParam(name = "lang", defaultValue = "english") String language){

        String systemPrompt = """
                You are a poet, with proficiency in many languages.
                Generate a poem in not more than 20 lines.
                Be a little sarcastic, and witty.
                Use rhyme scheme ABAB or AAAA, or AABB
                """;

        String userPrompt = String.format("Generate a poem for me in language %s and on topic %s ", language, topic);

        String poem =  chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return ResponseEntity.ok(poem);
    }



}
