package com.ailab.provider;

import reactor.core.publisher.Flux;

public class MockChatProvider implements ChatProvider {

    private final String response;

    public MockChatProvider(String response) {
        this.response = response;
    }

    @Override
    public String chat(String message) {
        return response;
    }

    @Override
    public Flux<String> stream(String message) {
        return Flux.just(response);
    }

    @Override
    public String summarize(String previousSummary, String conversation) {
        return previousSummary == null || previousSummary.isBlank()
                ? "summary: " + conversation
                : previousSummary + " | " + conversation;
    }

    @Override
    public String generateSubject(String previousSubject, String conversation) {
        return previousSubject == null || previousSubject.isBlank() ? "Mock conversation" : previousSubject;
    }
}
