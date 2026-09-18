package com.ailab.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

class KnowledgeRetrievalServiceTests {

    @Test
    void formatsRetrievedChunksAsPromptSources() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        KnowledgeChunkRepository repository = mock(KnowledgeChunkRepository.class);
        UUID documentId = UUID.randomUUID();
        when(repository.hasChunks()).thenReturn(true);
        when(embeddingModel.embed("question")).thenReturn(new float[] { 1.0f });
        when(repository.search(any(float[].class), org.mockito.ArgumentMatchers.eq(5),
                org.mockito.ArgumentMatchers.eq(0.25))).thenReturn(List.of(
                new RetrievedChunk(documentId, UUID.randomUUID(), "policy.pdf", 3,
                        "Refunds", "Refunds take five business days.", 0.91)));

        KnowledgeRetrievalService service = new KnowledgeRetrievalService(embeddingModel, repository, 5, 0.25);
        var context = service.retrieve("question");

        assertThat(context.promptSection()).contains("policy.pdf", "page 3", "Refunds take five business days");
        assertThat(context.sources()).singleElement().satisfies(source -> {
            assertThat(source.filename()).isEqualTo("policy.pdf");
            assertThat(source.pageNumber()).isEqualTo(3);
            assertThat(source.sectionTitle()).isEqualTo("Refunds");
        });
    }

    @Test
    void doesNotCallEmbeddingModelWhenThereAreNoChunks() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        KnowledgeChunkRepository repository = mock(KnowledgeChunkRepository.class);
        when(repository.hasChunks()).thenReturn(false);

        var context = new KnowledgeRetrievalService(embeddingModel, repository, 5, 0.25)
                .retrieve("question");

        assertThat(context.chunks()).isEmpty();
    }
}
