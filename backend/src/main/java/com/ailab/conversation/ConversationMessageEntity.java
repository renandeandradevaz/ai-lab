package com.ailab.conversation;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversation_messages")
public class ConversationMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conversationId;

    @Enumerated(EnumType.STRING)
    private MessageRole role;

    private String content;
    private int sequenceNumber;
    private Instant createdAt;

    protected ConversationMessageEntity() {
    }

    public ConversationMessageEntity(String conversationId, MessageRole role, String content,
            int sequenceNumber, Instant createdAt) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.sequenceNumber = sequenceNumber;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
