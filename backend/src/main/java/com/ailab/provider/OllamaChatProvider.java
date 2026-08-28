package com.ailab.provider;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class OllamaChatProvider implements ChatProvider {

    private final ChatClient chatClient;

    public OllamaChatProvider(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String message) {
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return content == null ? "" : content;
    }

    @Override
    public Flux<String> stream(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
