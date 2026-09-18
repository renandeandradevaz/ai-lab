package com.ailab.chat;

import com.ailab.conversation.ConversationMemoryService;
import com.ailab.knowledge.KnowledgeRetrievalService;
import com.ailab.knowledge.KnowledgeSource;
import com.ailab.provider.ChatProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ConversationChatService {

    private final ChatProvider chatProvider;
    private final ConversationMemoryService memoryService;
    private final KnowledgeRetrievalService knowledgeRetrievalService;

    public ConversationChatService(ChatProvider chatProvider, ConversationMemoryService memoryService,
            KnowledgeRetrievalService knowledgeRetrievalService) {
        this.chatProvider = chatProvider;
        this.memoryService = memoryService;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
    }

    public ChatResult chat(String userId, String conversationId, String message) {
        ConversationMemoryService.ConversationContext context = memoryService.begin(userId, conversationId, message);
        KnowledgeRetrievalService.KnowledgeContext knowledge = knowledgeRetrievalService.retrieve(message);
        String answer = chatProvider.chat(context.prompt() + knowledge.promptSection());
        ConversationMemoryService.Completion completion = finish(context, answer);
        return new ChatResult(completion.conversationId(), completion.subject(), completion.previousSummary(), answer,
                knowledge.sources());
    }

    public Flux<String> stream(String userId, String conversationId, String message) {
        ConversationMemoryService.ConversationContext context = memoryService.begin(userId, conversationId, message);
        KnowledgeRetrievalService.KnowledgeContext knowledge = knowledgeRetrievalService.retrieve(message);
        StringBuilder answer = new StringBuilder();
        return chatProvider.stream(context.prompt() + knowledge.promptSection())
                .doOnNext(answer::append)
                .doOnComplete(() -> finish(context, answer.toString()));
    }

    private ConversationMemoryService.Completion finish(
            ConversationMemoryService.ConversationContext context, String answer) {
        String conversation = context.prompt() + "\n\nASSISTANT: " + answer;
        String subject = safeSubject(context.subject(), conversation);
        ConversationMemoryService.Completion completion = memoryService.saveAssistant(
                context.userId(), context.conversationId(), answer, subject);

        String summary = completion.previousSummary();
        if (completion.shouldSummarize()) {
            try {
                summary = chatProvider.summarize(completion.previousSummary(), completion.transcript());
                if (summary != null && !summary.isBlank()) {
                    memoryService.replaceWithSummary(context.userId(), context.conversationId(),
                            completion.lastSequence(), summary);
                }
            } catch (RuntimeException ignored) {
                // Keep the messages when the optional compaction call fails.
            }
        }
        return new ConversationMemoryService.Completion(completion.conversationId(), completion.userId(),
                completion.subject(), summary, completion.lastSequence(), completion.shouldSummarize(),
                completion.transcript());
    }

    private String safeSubject(String previousSubject, String conversation) {
        try {
            String subject = chatProvider.generateSubject(previousSubject, conversation);
            return subject == null || subject.isBlank() ? previousSubject : subject;
        } catch (RuntimeException ignored) {
            return previousSubject;
        }
    }

    public record ChatResult(String conversationId, String subject, String summary, String message,
            java.util.List<KnowledgeSource> sources) {
    }
}
