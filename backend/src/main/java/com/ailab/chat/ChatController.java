package com.ailab.chat;

import com.ailab.provider.ChatProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatProvider chatProvider;

    public ChatController(ChatProvider chatProvider) {
        this.chatProvider = chatProvider;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatProvider.chat(request.message()));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@Valid @RequestBody ChatRequest request) {
        return chatProvider.stream(request.message());
    }

    public record ChatRequest(@NotBlank(message = "Message must not be blank") String message) {
    }

    public record ChatResponse(String message) {
    }
}
