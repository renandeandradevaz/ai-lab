package com.ailab.knowledge;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeDocumentRepository {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgeDocumentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<KnowledgeDocument> findByHash(String contentHash) {
        return jdbcTemplate.query(
                "SELECT * FROM knowledge_documents WHERE content_hash = ?",
                this::map,
                contentHash).stream().findFirst();
    }

    public Optional<KnowledgeDocument> findById(UUID id) {
        return jdbcTemplate.query(
                "SELECT * FROM knowledge_documents WHERE id = ?",
                this::map,
                id).stream().findFirst();
    }

    public List<KnowledgeDocument> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM knowledge_documents ORDER BY created_at DESC",
                this::map);
    }

    public void insert(UUID id, String filename, String hash, String storagePath) {
        jdbcTemplate.update("""
                INSERT INTO knowledge_documents (id, filename, content_hash, storage_path, status)
                VALUES (?, ?, ?, ?, ?)
                """, id, filename, hash, storagePath, KnowledgeDocumentStatus.PROCESSING.name());
    }

    public void markCompleted(UUID id, int pageCount, int chunkCount) {
        jdbcTemplate.update("""
                UPDATE knowledge_documents
                SET status = ?, page_count = ?, chunk_count = ?, error_message = NULL, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, KnowledgeDocumentStatus.COMPLETED.name(), pageCount, chunkCount, id);
    }

    public void markFailed(UUID id, String errorMessage) {
        jdbcTemplate.update("""
                UPDATE knowledge_documents
                SET status = ?, error_message = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, KnowledgeDocumentStatus.FAILED.name(), errorMessage, id);
    }

    public void delete(UUID id) {
        jdbcTemplate.update("DELETE FROM knowledge_documents WHERE id = ?", id);
    }

    private KnowledgeDocument map(ResultSet resultSet, int rowNumber) throws SQLException {
        return new KnowledgeDocument(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("filename"),
                resultSet.getString("content_hash"),
                resultSet.getString("storage_path"),
                KnowledgeDocumentStatus.valueOf(resultSet.getString("status")),
                resultSet.getInt("page_count"),
                resultSet.getInt("chunk_count"),
                resultSet.getString("error_message"),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class),
                resultSet.getObject("updated_at", java.time.OffsetDateTime.class));
    }
}
