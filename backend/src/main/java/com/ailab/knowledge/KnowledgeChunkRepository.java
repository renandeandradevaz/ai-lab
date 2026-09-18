package com.ailab.knowledge;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteByDocumentId(UUID documentId) {
        jdbcTemplate.update("DELETE FROM knowledge_chunks WHERE document_id = ?", documentId);
    }

    public boolean hasChunks() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM knowledge_chunks", Integer.class);
        return count != null && count > 0;
    }

    public void insert(UUID id, UUID documentId, int chunkIndex, int pageNumber,
            String sectionTitle, String content, String metadataJson, float[] embedding) {
        jdbcTemplate.update("""
                INSERT INTO knowledge_chunks
                    (id, document_id, chunk_index, page_number, section_title, content, metadata, embedding)
                VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?::vector)
                """, id, documentId, chunkIndex, pageNumber, sectionTitle, content,
                metadataJson, vectorLiteral(embedding));
    }

    public List<RetrievedChunk> search(float[] embedding, int topK, double minimumSimilarity) {
        String vector = vectorLiteral(embedding);
        return jdbcTemplate.query("""
                SELECT c.document_id, c.id, d.filename, c.page_number, c.section_title, c.content,
                       1 - (c.embedding <=> CAST(? AS vector)) AS similarity
                FROM knowledge_chunks c
                JOIN knowledge_documents d ON d.id = c.document_id
                WHERE d.status = 'COMPLETED'
                  AND 1 - (c.embedding <=> CAST(? AS vector)) >= ?
                ORDER BY c.embedding <=> CAST(? AS vector)
                LIMIT ?
                """, this::map, vector, vector, minimumSimilarity, vector, topK);
    }

    private RetrievedChunk map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new RetrievedChunk(
                resultSet.getObject("document_id", UUID.class),
                resultSet.getObject("id", UUID.class),
                resultSet.getString("filename"),
                resultSet.getInt("page_number"),
                resultSet.getString("section_title"),
                resultSet.getString("content"),
                resultSet.getDouble("similarity"));
    }

    private static String vectorLiteral(float[] embedding) {
        StringBuilder value = new StringBuilder("[");
        for (int index = 0; index < embedding.length; index++) {
            if (index > 0) {
                value.append(',');
            }
            value.append(embedding[index]);
        }
        return value.append(']').toString();
    }
}
