package com.ailab.knowledge;

import java.time.OffsetDateTime;
import java.util.UUID;

public record KnowledgeDocument(
        UUID id,
        String filename,
        String contentHash,
        String storagePath,
        KnowledgeDocumentStatus status,
        int pageCount,
        int chunkCount,
        String errorMessage,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
