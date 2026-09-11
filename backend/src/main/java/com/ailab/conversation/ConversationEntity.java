package com.ailab.conversation;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversations")
public class ConversationEntity {

    @Id
    private String id;
    private String userId;
    private String subject;
    private String summary;
    private int messageCount;
    private int lastSequence;
    private Instant createdAt;
    private Instant updatedAt;

    protected ConversationEntity() {
    }

    public ConversationEntity(String id, String userId, String subject, Instant now) {
        this.id = id;
        this.userId = userId;
        this.subject = subject;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getSubject() {
        return subject;
    }

    public String getSummary() {
        return summary;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getLastSequence() {
        return lastSequence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void recordMessages(int count, int lastSequence, String subject, Instant now) {
        this.messageCount = count;
        this.lastSequence = lastSequence;
        this.subject = subject;
        this.updatedAt = now;
    }

    public void replaceSummary(String summary, int remainingMessages, Instant now) {
        this.summary = summary;
        this.messageCount = remainingMessages;
        this.updatedAt = now;
    }
}
