package com.ailab.knowledge;

public record KnowledgeSource(
        String filename,
        int pageNumber,
        String sectionTitle,
        double similarity) {
}
