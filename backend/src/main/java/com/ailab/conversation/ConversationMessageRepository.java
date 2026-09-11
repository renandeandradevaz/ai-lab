package com.ailab.conversation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessageEntity, Long> {

    List<ConversationMessageEntity> findByConversationIdOrderBySequenceNumberAsc(String conversationId);

    long countByConversationId(String conversationId);

    void deleteByConversationIdAndSequenceNumberLessThanEqual(String conversationId, int sequenceNumber);
}
