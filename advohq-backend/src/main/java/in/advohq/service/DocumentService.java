package in.advohq.service;

import in.advohq.domain.Document;
import in.advohq.dto.DocumentResponse;
import in.advohq.dto.DownloadUrlResponse;
import in.advohq.exception.NotFoundException;
import in.advohq.repo.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documents;
    private final S3StorageService storage;

    public DocumentService(DocumentRepository documents, S3StorageService storage) {
        this.documents = documents;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(UUID userId, UUID caseId) {
        List<Document> found = (caseId != null)
                ? documents.findByUserIdAndCaseIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, caseId)
                : documents.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        return found.stream().map(DocumentResponse::from).toList();
    }

    /** Documents currently in the Trash. */
    @Transactional(readOnly = true)
    public List<DocumentResponse> listTrash(UUID userId) {
        return documents.findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(userId)
                .stream().map(DocumentResponse::from).toList();
    }

    /** Move a document to the Trash (soft delete — bytes stay in S3, recoverable). */
    @Transactional
    public DocumentResponse moveToTrash(UUID userId, UUID docId) {
        Document doc = require(userId, docId);
        if (doc.getDeletedAt() == null) {
            doc.setDeletedAt(java.time.Instant.now());
            documents.save(doc);
        }
        return DocumentResponse.from(doc);
    }

    /** Restore a document from the Trash back into the Library. */
    @Transactional
    public DocumentResponse restore(UUID userId, UUID docId) {
        Document doc = require(userId, docId);
        doc.setDeletedAt(null);
        documents.save(doc);
        return DocumentResponse.from(doc);
    }

    @Transactional
    public DocumentResponse upload(UUID userId, UUID caseId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }
        String original = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename() : "document";
        String key = buildKey(userId, original);

        try (var in = file.getInputStream()) {
            storage.upload(key, in, file.getSize(),
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        }

        Document doc = new Document();
        doc.setUserId(userId);
        doc.setCaseId(caseId);
        doc.setFileName(original);
        doc.setContentType(file.getContentType());
        doc.setKind(detectKind(original, file.getContentType()));
        doc.setSizeBytes(file.getSize());
        doc.setS3Key(key);
        return DocumentResponse.from(documents.save(doc));
    }

    @Transactional(readOnly = true)
    public DownloadUrlResponse downloadUrl(UUID userId, UUID docId, boolean inline) {
        Document doc = require(userId, docId);
        var url = storage.presignDownload(doc.getS3Key(), doc.getFileName(), inline);
        return new DownloadUrlResponse(url.url(), url.expiresAt());
    }

    /** File bytes plus the metadata a controller needs to stream them out. */
    public record FileContent(java.io.InputStream stream, String fileName, String contentType, long sizeBytes) {}

    /**
     * Streams the file's bytes through the backend — the browser can't fetch
     * the S3 URL directly (no CORS on the bucket), but the editor needs the
     * raw bytes to render DOCX files.
     */
    @Transactional(readOnly = true)
    public FileContent content(UUID userId, UUID docId) {
        Document doc = require(userId, docId);
        return new FileContent(
                storage.download(doc.getS3Key()),
                doc.getFileName(),
                doc.getContentType() != null ? doc.getContentType() : "application/octet-stream",
                doc.getSizeBytes());
    }

    /**
     * Replaces the bytes of an existing document in place (same id, same S3
     * key) with an edited version from the editor. The filename and kind are
     * preserved; only the stored content, size, and content type change.
     */
    @Transactional
    public DocumentResponse replaceContent(UUID userId, UUID docId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }
        Document doc = require(userId, docId);
        String contentType = file.getContentType() != null ? file.getContentType() : doc.getContentType();
        try (var in = file.getInputStream()) {
            storage.upload(doc.getS3Key(), in, file.getSize(),
                    contentType != null ? contentType : "application/octet-stream");
        }
        doc.setContentType(contentType);
        doc.setSizeBytes(file.getSize());
        return DocumentResponse.from(documents.save(doc));
    }

    @Transactional
    public void delete(UUID userId, UUID docId) {
        Document doc = require(userId, docId);
        storage.delete(doc.getS3Key());     // remove from S3 first
        documents.delete(doc);              // then drop metadata
    }

    private Document require(UUID userId, UUID docId) {
        return documents.findByIdAndUserId(docId, userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    /** Namespaced, collision-proof object key: docs/{userId}/{uuid}/{filename}. */
    private String buildKey(UUID userId, String fileName) {
        String safe = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "docs/%s/%s/%s".formatted(userId, UUID.randomUUID(), safe);
    }

    private String detectKind(String name, String contentType) {
        String lower = name.toLowerCase();
        String ct = contentType == null ? "" : contentType.toLowerCase();
        if (lower.endsWith(".pdf") || ct.equals("application/pdf")) return "pdf";
        if (lower.endsWith(".docx") || lower.endsWith(".doc") || ct.contains("word")) return "docx";
        if (ct.startsWith("image/") || lower.matches(".*\\.(png|jpe?g|gif|webp|bmp|svg)$")) return "image";
        return "other";
    }
}
