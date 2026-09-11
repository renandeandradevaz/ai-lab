package com.ailab.conversation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConversationMemoryTests {

    @Autowired
    private ConversationMemoryService memoryService;

    @Autowired
    private ConversationRepository conversationRepository;

    private String conversationId;

    @AfterEach
    void cleanUp() {
        if (conversationId != null) {
            conversationRepository.deleteById(conversationId);
        }
    }

    @Test
    void storesAndLoadsConversationForItsUser() {
        conversationId = UUID.randomUUID().toString();

        var context = memoryService.begin("user_1", conversationId, "Where is my order?");
        memoryService.saveAssistant("user_1", conversationId, "It is delayed.", "Delayed order");

        var conversation = memoryService.get("user_1", conversationId);

        assertThat(context.transcript()).contains("USER: Where is my order?");
        assertThat(conversation.subject()).isEqualTo("Delayed order");
        assertThat(conversation.messages()).hasSize(2);
    }

    @Test
    void compactsTwentyMessagesAndKeepsTheSummary() {
        conversationId = UUID.randomUUID().toString();

        for (int turn = 1; turn <= 10; turn++) {
            memoryService.begin("user_2", conversationId, "Question " + turn);
            var completion = memoryService.saveAssistant("user_2", conversationId,
                    "Answer " + turn, "Conversation subject");
            if (turn < 10) {
                assertThat(completion.shouldSummarize()).isFalse();
            } else {
                assertThat(completion.shouldSummarize()).isTrue();
                memoryService.replaceWithSummary("user_2", conversationId,
                        completion.lastSequence(), "Summary of the conversation");
            }
        }

        var conversation = memoryService.get("user_2", conversationId);

        assertThat(conversation.summary()).isEqualTo("Summary of the conversation");
        assertThat(conversation.messages()).isEmpty();
    }
}
