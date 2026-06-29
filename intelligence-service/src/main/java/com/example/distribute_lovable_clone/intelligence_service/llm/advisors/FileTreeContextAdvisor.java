package com.example.distribute_lovable_clone.intelligence_service.llm.advisors;

import com.example.distribute_lovable_clone.intelligence_service.client.WorkspaceClient;
import com.example.distributelovableclone.commonlib.dto.FileNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileTreeContextAdvisor implements StreamAdvisor {

    private final WorkspaceClient workspaceClient;

    //modifying request by, adding file-tree to the context
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Map<String,Object> context = chatClientRequest.context();
        Long projectId = Long.parseLong(context.getOrDefault("projectId",0).toString());

        ChatClientRequest augmentedChatClientRequest = augmentRequestWithFileTree(chatClientRequest, projectId);
        return streamAdvisorChain.nextStream(augmentedChatClientRequest);
    }

    //mutating the request to append file tree at the end, else it adds it at the beginning. This way we're saving
    //some token cost, trying to use AI caching
    ChatClientRequest augmentRequestWithFileTree(ChatClientRequest chatClientRequest, Long projectId){

        List<Message> incomingMessages = chatClientRequest.prompt().getInstructions();

        Message systemMessage = incomingMessages.stream()
                .filter(m->m.getMessageType()== MessageType.SYSTEM)
                .findFirst()
                .orElse(null);

        List<Message> userMessages = incomingMessages.stream()
                .filter(m->m.getMessageType()!=MessageType.SYSTEM)
                .toList();

        List<Message> allMessages = new ArrayList<>();

        //adding original system_message
        if(systemMessage!=null)
            allMessages.add(systemMessage);

        List<FileNode> fileTree = workspaceClient.getFileTree(projectId).files();
        String fileTreeContext  = "\n\n ---- You can use this FILE_TREE to choose which files to read from ----\n"+fileTree.toString();


        //adding file tree context to user_messages
        allMessages.add(new UserMessage(fileTreeContext));
        allMessages.addAll(userMessages);

        return chatClientRequest
                .mutate()
                .prompt(new Prompt(allMessages, chatClientRequest.prompt().getOptions()))
                .build();
    }

    @Override
    public String getName() {
        return "FileTreeContextAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
