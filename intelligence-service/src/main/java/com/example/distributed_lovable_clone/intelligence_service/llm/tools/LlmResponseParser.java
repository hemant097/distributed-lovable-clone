package com.example.distributed_lovable_clone.intelligence_service.llm.tools;

import com.example.distributed_lovable_clone.common_lib.enums.ChatEventStatus;
import com.example.distributed_lovable_clone.intelligence_service.entity.ChatEvent;
import com.example.distributed_lovable_clone.intelligence_service.entity.ChatMessage;
import com.example.distributed_lovable_clone.common_lib.enums.ChatEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class LlmResponseParser {

        /**
         * Regex Breakdown:
         * Group_1: Opening Tag i.e. <tag_name...>
         * Group_2: Tag Name (message|file|tool)
         * Group_3: Attribute's part ( anything that isn't '>' e.g.,  id="1" , name="doc.pdf" ,etc.)
         * Group_4: Content inside the tag, lazy (The stuff inside)
         * Group_5: Closing Tag, using a backreference to whatever(message|file|tool) matched in Group_2 (</tag_name>)
         * <p>
         * Why [\s\S]*? and not just .*
         * . does not match newlines in Java by default
         * [\s\S] means whitespace or non-whitespace = literally any character including newlines
         * *? is lazy — matches as little as possible, so it stops at the first closing tag it finds
         * <p>
         * Flag                             Meaning
         * Pattern.CASE_INSENSITIVE     Message and message both match
         * Pattern.DOTALL               . matches newlines too, making [\s\S] redundant here
         * <p>
         * as we're using DOTALL can simply use .*? instead of [\\s\\S]*?,
         * or we can remove the DOTALL and keep using [\\s\\S]
         *
         */
        private static final Pattern GENERIC_TAG_PATTERN = Pattern.compile(
                "(<(message|file|tool)([^>]*)>)([\\s\\S]*?)(</\\2>)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );


        // Helper to extract specific attributes (path="..." or args="...") from Group 3
        private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
                "(path|args)=\"([^\"]+)\""
        );

        public List<ChatEvent> parseChatEvents(String fullResponse, ChatMessage parentMessage) {
            List<ChatEvent> events = new ArrayList<>();
            int orderCounter = 1;

            Matcher matcher = GENERIC_TAG_PATTERN.matcher(fullResponse);

            while (matcher.find()) {
                String tagName = matcher.group(2).toLowerCase();
                String attributes = matcher.group(3);
                String content = matcher.group(4).trim();

                // Extract attributes map
                Map<String, String> attrMap = extractAttributes(attributes);

                ChatEvent.ChatEventBuilder builder = ChatEvent.builder()
                        .status(ChatEventStatus.CONFIRMED)
                        .chatMessage(parentMessage)
                        .content(content) // for Markdown content
                        .sequenceOrder(orderCounter++);

                switch (tagName) {
                    case "message" -> builder.type(ChatEventType.MESSAGE);
                    case "file" -> {
                        builder.type(ChatEventType.FILE_EDIT);
                        builder.status(ChatEventStatus.PENDING);
                        builder.filePath(attrMap.get("path")); // Required for files
//                    builder.content(null);
                    }
                    case "tool" -> {
                        builder.type(ChatEventType.TOOL_LOG);
                        builder.metadata(attrMap.get("args")); // Store raw file list in metadata
                    }
                    default -> {continue;}
                }

                events.add(builder.build());
            }
            //TODO: add tokens used logic
            log.info("All events parsed for assistant response, tokens used: {}",parentMessage.getTokensUsed());
            return events;
        }

        private Map<String, String> extractAttributes(String attributeString) {
            Map<String, String> attributes = new HashMap<>();
            if (attributeString == null) return attributes;

            Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
            while (matcher.find()) {
                attributes.put(matcher.group(1), matcher.group(2));
            }
            return attributes;
        }

    }

