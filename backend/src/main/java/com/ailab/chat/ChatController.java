package com.ailab.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import com.ailab.conversation.ConversationMemoryService;
import com.ailab.knowledge.KnowledgeSource;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ConversationChatService chatService;
    private final ConversationMemoryService memoryService;

    public ChatController(ConversationChatService chatService, ConversationMemoryService memoryService) {
        this.chatService = chatService;
        this.memoryService = memoryService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        ConversationChatService.ChatResult result = chatService.chat(
                request.userId(), request.conversationId(), request.message());
        return new ChatResponse(result.conversationId(), result.subject(), result.summary(), result.message(),
                result.sources());
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@Valid @RequestBody ChatRequest request) {
        return chatService.stream(request.userId(), request.conversationId(), request.message());
    }

    @GetMapping("/conversations")
    public java.util.List<ConversationMemoryService.ConversationSummary> conversations(@RequestParam String userId) {
        return memoryService.list(userId);
    }

    @GetMapping("/conversations/{conversationId}")
    public ConversationMemoryService.ConversationView conversation(
            @PathVariable String conversationId, @RequestParam String userId) {
        return memoryService.get(userId, conversationId);
    }

    public record ChatRequest(
            @NotBlank(message = "User ID must not be blank") String userId,
            @NotNull(message = "Conversation ID must not be null") String conversationId,
            @NotBlank(message = "Message must not be blank")
            @Size(max = 500, message = "Message must not exceed 500 characters") String message) {
    }

    public record ChatResponse(String conversationId, String subject, String summary, String message,
            java.util.List<KnowledgeSource> sources) {
    }
}
