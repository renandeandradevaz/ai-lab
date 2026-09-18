package com.ailab.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PdfChunkerTests {

    private final PdfChunker chunker = new PdfChunker(50, 10);

    @Test
    void keepsNumberedSectionsAndPageMetadata() {
        List<PdfChunk> chunks = chunker.chunkPage("""
                1. Prazos estimados

                A entrega normal leva cinco dias uteis.

                2. Pedido atrasado

                Um pedido e atrasado quando ultrapassa a data estimada.
                """, 4);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).pageNumber()).isEqualTo(4);
        assertThat(chunks.get(0).sectionTitle()).isEqualTo("1. Prazos estimados");
        assertThat(chunks.get(0).content()).contains("cinco dias uteis");
        assertThat(chunks.get(1).sectionTitle()).isEqualTo("2. Pedido atrasado");
    }

    @Test
    void splitsLongParagraphsWithOverlap() {
        String paragraph = "word ".repeat(120);

        List<PdfChunk> chunks = chunker.chunkPage(paragraph, 1);

        assertThat(chunks).hasSizeGreaterThan(2);
        assertThat(chunks.get(0).content().split(" ")).hasSize(50);
        assertThat(chunks.get(1).content()).contains("word word word");
    }
}
