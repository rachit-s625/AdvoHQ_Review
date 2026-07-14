package in.advohq.dto;

import in.advohq.domain.Document;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID caseId,
        String fileName,
        String contentType,
        String kind,
        long sizeBytes,
        Instant createdAt
) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(
                d.getId(), d.getCaseId(), d.getFileName(), d.getContentType(),
                d.getKind(), d.getSizeBytes(), d.getCreatedAt());
    }
}
