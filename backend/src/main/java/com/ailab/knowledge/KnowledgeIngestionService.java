package com.ailab.knowledge;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class KnowledgeIngestionService {

    private final EmbeddingModel embeddingModel;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final PdfChunker chunker;
    private final Path storageDirectory;

    public KnowledgeIngestionService(
            EmbeddingModel embeddingModel,
            KnowledgeDocumentRepository documentRepository,
            KnowledgeChunkRepository chunkRepository,
            PdfChunker chunker,
            @Value("${ailab.knowledge.storage-path:./knowledge-documents}") String storagePath) {
        this.embeddingModel = embeddingModel;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.chunker = chunker;
        this.storageDirectory = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    public KnowledgeDocument ingest(MultipartFile file) {
        validate(file);
        String filename = safeFilename(file.getOriginalFilename());
        byte[] bytes = read(file);
        String hash = sha256(bytes);

        var existing = documentRepository.findByHash(hash);
        if (existing.isPresent() && existing.get().status() != KnowledgeDocumentStatus.FAILED) {
            return existing.get();
        }
        existing.ifPresent(document -> documentRepository.delete(document.id()));

        UUID documentId = UUID.randomUUID();
        Path storedFile = storageDirectory.resolve(documentId + "-" + filename).normalize();
        try {
            Files.createDirectories(storageDirectory);
            Files.write(storedFile, bytes);
            documentRepository.insert(documentId, filename, hash, storedFile.toString());
            processPdf(documentId, filename, storedFile);
            return documentRepository.findById(documentId).orElseThrow();
        } catch (RuntimeException | IOException exception) {
            documentRepository.markFailed(documentId, safeMessage(exception));
            throw new KnowledgeIngestionException("Could not ingest PDF " + filename, exception);
        }
    }

    public List<KnowledgeDocument> list() {
        return documentRepository.findAll();
    }

    public KnowledgeDocument get(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException(id));
    }

    @Transactional
    public void delete(UUID id) {
        KnowledgeDocument document = get(id);
        documentRepository.delete(id);
        try {
            Files.deleteIfExists(Paths.get(document.storagePath()));
        } catch (IOException exception) {
            throw new KnowledgeIngestionException("Could not delete stored PDF", exception);
        }
    }

    private void processPdf(UUID documentId, String filename, Path storedFile) throws IOException {
        List<PdfChunk> chunks = new ArrayList<>();
        int pageCount;
        try (PDDocument pdf = PDDocument.load(storedFile.toFile())) {
            pageCount = pdf.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                chunks.addAll(chunker.chunkPage(stripper.getText(pdf), page));
            }
        }

        if (chunks.isEmpty()) {
            throw new KnowledgeIngestionException("PDF does not contain extractable text");
        }

        chunkRepository.deleteByDocumentId(documentId);
        for (int index = 0; index < chunks.size(); index++) {
            PdfChunk chunk = chunks.get(index);
            float[] embedding = embeddingModel.embed(chunk.content());
            String metadata = "{\"filename\":" + quote(filename)
                    + ",\"pageNumber\":" + chunk.pageNumber()
                    + ",\"sectionTitle\":" + quote(chunk.sectionTitle())
                    + ",\"chunkIndex\":" + index + "}";
            chunkRepository.insert(UUID.randomUUID(), documentId, index, chunk.pageNumber(),
                    chunk.sectionTitle(), chunk.content(), metadata, embedding);
        }
        documentRepository.markCompleted(documentId, pageCount, chunks.size());
    }

    private static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new KnowledgeIngestionException("A non-empty PDF file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            throw new KnowledgeIngestionException("Only PDF files are supported");
        }
    }

    private static byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new KnowledgeIngestionException("Could not read uploaded file", exception);
        }
    }

    private static String safeFilename(String filename) {
        String cleaned = StringUtils.cleanPath(filename == null ? "document.pdf" : filename);
        String basename = Paths.get(cleaned).getFileName().toString();
        return basename.isBlank() ? "document.pdf" : basename;
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null ? exception.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 1000));
    }

    public static class KnowledgeIngestionException extends RuntimeException {
        public KnowledgeIngestionException(String message) {
            super(message);
        }

        public KnowledgeIngestionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class KnowledgeDocumentNotFoundException extends RuntimeException {
        public KnowledgeDocumentNotFoundException(UUID id) {
            super("Knowledge document not found: " + id);
        }
    }
}
