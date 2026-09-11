package com.ailab.provider;

import reactor.core.publisher.Flux;

public interface ChatProvider {

    String chat(String message);

    Flux<String> stream(String message);

    String summarize(String previousSummary, String conversation);

    String generateSubject(String previousSubject, String conversation);
}
