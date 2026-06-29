package com.example.distribute_lovable_clone.intelligence_service.service.impl;

import com.example.distribute_lovable_clone.intelligence_service.dto.chat.StreamResponse;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatEvent;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatMessage;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatSession;
import com.example.distribute_lovable_clone.intelligence_service.entity.ChatSessionId;
import com.example.distribute_lovable_clone.intelligence_service.llm.PromptUtil;
import com.example.distribute_lovable_clone.intelligence_service.llm.advisors.FileTreeContextAdvisor;
import com.example.distribute_lovable_clone.intelligence_service.llm.tools.CodeGenerationTool;
import com.example.distribute_lovable_clone.intelligence_service.llm.tools.LlmResponseParser;
import com.example.distribute_lovable_clone.intelligence_service.repository.ChatEventRepository;
import com.example.distribute_lovable_clone.intelligence_service.repository.ChatMessageRepository;
import com.example.distribute_lovable_clone.intelligence_service.repository.ChatSessionRepository;
import com.example.distribute_lovable_clone.intelligence_service.service.AiGenerationService;
import com.example.distribute_lovable_clone.intelligence_service.service.UsageService;
import com.example.distributelovableclone.commonlib.enums.ChatEventType;
import com.example.distributelovableclone.commonlib.enums.MessageRole;
import com.example.distributelovableclone.commonlib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ChatSessionRepository chatSessionRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final ChatEventRepository chatEventRepo;
    private final LlmResponseParser llmResponseParser;
    private final UsageService usageService;

    /**Explanation:
     For using \ before " -> \" is escaped double quote. If we want real " inside string we need to put \" or for \ we need \\
     So the actual regex becomes "<file path="([^"]+)">(.*?)</file>"
      1-> <file path="
            - Matches this text literally.

      2-> ([^"]+) denotes Group 1
            [ ] → character set.
            ^" → any character except "
            + → repeat 1 or more times
            So it captures everything until the next double quote.
            e.g., <file path="notes/test.txt"> captures notes/test.txt

      3-> ">
            Matches the "(closing quote) and > literally.

      4-> (.*?) denotes Group 2.
            . → any character.
            * → repeat 0 or more times.
            ? → non-greedy (stop as soon as possible).
            Captures content inside <file>...</file>.

      5-> </file>
            Matches closing tag literally.

      6-> Pattern.DOTALL
            Makes . also match newline characters.
            Allows multiline content inside the file tag.
      Complete example:
      <file path="notes.txt">
        Hello
        World
       </file>

       Results:

       matcher.group(1) → notes.txt
       matcher.group(2) → Hello
                          World


    * */
    private final Pattern FILE_TAG_PATTERN = Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>",Pattern.DOTALL);


    @Override
    @PreAuthorize("@security.canEditTheProject(#projectId)")
    public Flux<StreamResponse> streamResponse(String userMessage, Long projectId) {

//        usageService.checkDailyTokensUsage();

        Long userId = authUtil.getCurrentUserId();
        ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);

        //creating context to pass as AdvisorSpec params
        Map<String,Object> advisorParams = Map.of(
                "userId",userId,
                "projectId",projectId
        );

        StringBuilder fullResponseBuffer = new StringBuilder();
        CodeGenerationTool codeGenerationTools = new CodeGenerationTool(projectId);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();

        return chatClient.prompt()
                .system(PromptUtil.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTools) //tool to request the required file and it's content
                .advisors( advisorSpec -> {
                    advisorSpec.params(advisorParams);
                    advisorSpec.advisors(fileTreeContextAdvisor); //advisor to go through the file tree
                        })
                .stream()
                .chatResponse()
                .doOnNext( response ->{
                    String content = response.getResult().getOutput().getText();

                    if(content!=null && !content.isEmpty() && endTime.get()==0){
                        endTime.set(System.currentTimeMillis()); //capturing time when first non-empty chunk is received
                    }

                    //setting Usage from the response metadata
                    usageRef.set(response.getMetadata().getUsage());

                    fullResponseBuffer.append(content);
                })
                .doOnComplete( () -> {
                    Schedulers.boundedElastic().schedule(() -> {
//                                parseAndSaveFiles(fullResponseBuffer.toString(), projectId);

                                long duration = (endTime.get() - startTime.get())/1000;

                                finalizeChats(userMessage,chatSession,fullResponseBuffer.toString(),duration,usageRef.get());
                            }
                    );
                })
                .doOnError( error -> log.error("Error during streaming for projectId:{}",projectId))
                .map( response -> {
                    String text = Objects.requireNonNull(response.getResult()).getOutput().getText();
                    return new StreamResponse(text != null ? text : "");
                })
                ;
    }

    private void finalizeChats(String userMessage, ChatSession chatSession, String fullText,Long duration,Usage promptUsage){
        Long projectId = chatSession.getId().getProjectId();

        //saving tokens used
        if(promptUsage!=null){
            int totalTokens = promptUsage.getTotalTokens();
            usageService.recordTokenUsage(chatSession.getId().getUserId(), totalTokens);
        }

        //saving user message
        chatMessageRepo.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .tokensUsed(promptUsage.getPromptTokens())
                        .build()
        );

        ChatMessage assistantChatMessage = chatMessageRepo.save(
                ChatMessage.builder()
                .chatSession(chatSession)
                .role(MessageRole.ASSISTANT)
                .tokensUsed(promptUsage.getCompletionTokens())
                .build()
        );
        log.info("User and assistant message saved for {}",chatSession.getId());

        List<ChatEvent> chatEventList = llmResponseParser.parseChatEvents(fullText, assistantChatMessage);
        chatEventList.addFirst(ChatEvent.builder()
                        .type(ChatEventType.THOUGHT)
                        .chatMessage(assistantChatMessage)
                        .content("Thought for "+duration+"s")
                        .sequenceOrder(0)
                .build());

        chatEventList.stream()
                .filter( chatEvent -> chatEvent.getType() == ChatEventType.FILE_EDIT)
                .forEach( fileEditEvent ->{
                    String filePath = fileEditEvent.getFilePath();
                    String content = fileEditEvent.getContent();
//                    projectFileService.saveFile(projectId, filePath,content); TODO -> add Kafka
                });

        log.info("All events saved for {}",chatSession.getId());
        chatEventRepo.saveAll(chatEventList);

    }

    private void parseAndSaveFiles(String fullResponse, Long projectId) {
//        String dummy = """
//                <message> I'm going to read the files and generate code</message>
//                <file path="src/App.jsx">
//                    import App from './App.jsx'
//                    ......
//                </file>
//                <message> I'm going to read the files and generate code</message>
//                <file path="src/App.jsx">
//                    import App from './App.jsx'
//                    ...... 
//                </file>
//                """;
        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
        while (matcher.find()){
            String filePath = matcher.group(1);
            String fileContent = matcher.group(2);

            projectFileService.saveFile(projectId, filePath, fileContent);
        }
        log.info("All files parsed and saved for projectId:{}",projectId);
    }

    private ChatSession createChatSessionIfNotExists(Long projectId, Long userId){
        ChatSessionId chatSessionId = new ChatSessionId(projectId,userId);
        ChatSession chatSession = chatSessionRepo.findById(chatSessionId).orElse(null);
        if(chatSession == null) {
            chatSession = ChatSession.builder()
                    .id(chatSessionId)
                    .build();

            log.info("new chat session created for chatSessionId: {}", chatSessionId);
            return chatSessionRepo.save(chatSession);
        }
        log.info("chat session already exists for chatSessionId: {}", chatSessionId);

        return chatSession;
    }
}
