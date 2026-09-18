package com.ailab.knowledge;

import java.util.UUID;

public record RetrievedChunk(
        UUID documentId,
        UUID chunkId,
        String filename,
        int pageNumber,
        String sectionTitle,
        String content,
        double similarity) {
}
