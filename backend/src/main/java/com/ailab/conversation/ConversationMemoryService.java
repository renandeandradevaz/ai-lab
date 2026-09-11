package com.ailab.conversation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationMemoryService {

    private static final String DEFAULT_SUBJECT = "New conversation";
    private static final int SUMMARY_MESSAGE_LIMIT = 20;
    private static final Pattern USER_ID = Pattern.compile("user_[123]");

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository messageRepository;

    public ConversationMemoryService(ConversationRepository conversationRepository,
            ConversationMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ConversationContext begin(String userId, String conversationId, String message) {
        validateUser(userId);
        String id = validateConversationId(conversationId);
        ConversationEntity conversation = conversationRepository.findById(id)
                .map(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        throw notFound("Conversation was not found.");
                    }
                    return conversationRepository.findForUpdate(id, userId).orElseThrow();
                })
                .orElseGet(() -> createConversation(id, userId));

        int sequence = conversation.getLastSequence() + 1;
        messageRepository.save(new ConversationMessageEntity(
                id, MessageRole.USER, message, sequence, Instant.now()));
        conversation.recordMessages(conversation.getMessageCount() + 1, sequence, conversation.getSubject(), Instant.now());
        conversationRepository.save(conversation);
        return contextFor(conversation);
    }

    @Transactional
    public Completion saveAssistant(String userId, String conversationId, String answer, String subject) {
        ConversationEntity conversation = conversationRepository.findForUpdate(conversationId, userId)
                .orElseThrow(() -> notFound("Conversation was not found."));
        int sequence = conversation.getLastSequence() + 1;
        messageRepository.save(new ConversationMessageEntity(
                conversationId, MessageRole.ASSISTANT, answer, sequence, Instant.now()));
        String safeSubject = subject == null || subject.isBlank() ? conversation.getSubject() : truncate(subject);
        conversation.recordMessages(conversation.getMessageCount() + 1, sequence, safeSubject, Instant.now());
        conversationRepository.save(conversation);

        List<ConversationMessageEntity> messages = messageRepository
                .findByConversationIdOrderBySequenceNumberAsc(conversationId);
        return new Completion(conversationId, userId, safeSubject, conversation.getSummary(), sequence,
                messages.size() >= SUMMARY_MESSAGE_LIMIT, transcript(messages));
    }

    @Transactional
    public void replaceWithSummary(String userId, String conversationId, int lastSequence, String summary) {
        ConversationEntity conversation = conversationRepository.findForUpdate(conversationId, userId)
                .orElseThrow(() -> notFound("Conversation was not found."));
        messageRepository.deleteByConversationIdAndSequenceNumberLessThanEqual(conversationId, lastSequence);
        int remaining = (int) messageRepository.countByConversationId(conversationId);
        conversation.replaceSummary(summary, remaining, Instant.now());
        conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationSummary> list(String userId) {
        validateUser(userId);
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(conversation -> new ConversationSummary(
                        conversation.getId(), conversation.getSubject(), conversation.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationView get(String userId, String conversationId) {
        validateUser(userId);
        ConversationEntity conversation = conversationRepository.findByIdAndUserId(
                        validateConversationId(conversationId), userId)
                .orElseThrow(() -> notFound("Conversation was not found."));
        List<MessageView> messages = messageRepository.findByConversationIdOrderBySequenceNumberAsc(conversation.getId())
                .stream()
                .map(message -> new MessageView(message.getRole().name(), message.getContent()))
                .toList();
        return new ConversationView(conversation.getId(), conversation.getUserId(), conversation.getSubject(),
                conversation.getSummary(), messages);
    }

    private ConversationEntity createConversation(String id, String userId) {
        return conversationRepository.save(new ConversationEntity(id, userId, DEFAULT_SUBJECT, Instant.now()));
    }

    private ConversationContext contextFor(ConversationEntity conversation) {
        List<ConversationMessageEntity> messages = messageRepository
                .findByConversationIdOrderBySequenceNumberAsc(conversation.getId());
        return new ConversationContext(conversation.getId(), conversation.getUserId(), conversation.getSubject(),
                conversation.getSummary(), transcript(messages));
    }

    private String transcript(List<ConversationMessageEntity> messages) {
        return messages.stream()
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private String validateConversationId(String conversationId) {
        try {
            return UUID.fromString(conversationId).toString();
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation ID must be a UUID.");
        }
    }

    private void validateUser(String userId) {
        if (userId == null || !USER_ID.matcher(userId).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be user_1, user_2, or user_3.");
        }
    }

    private String truncate(String value) {
        return value.length() <= 80 ? value : value.substring(0, 80);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    public record ConversationContext(String conversationId, String userId, String subject,
            String summary, String transcript) {

        public String prompt() {
            String prior = summary == null || summary.isBlank() ? "No previous summary." : summary;
            return "Conversation summary:\n" + prior + "\n\nConversation messages:\n" + transcript;
        }
    }

    public record Completion(String conversationId, String userId, String subject, String previousSummary,
            int lastSequence, boolean shouldSummarize, String transcript) {
    }

    public record ConversationSummary(String id, String subject, Instant updatedAt) {
    }

    public record ConversationView(String id, String userId, String subject, String summary,
            List<MessageView> messages) {
    }

    public record MessageView(String role, String content) {
    }
}
