package in.advohq.repo;

import in.advohq.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /** All documents for a user, including trashed — used for full account cleanup. */
    List<Document> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Active (non-trashed) documents in the Library. */
    List<Document> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    /** Active (non-trashed) documents in a given case. */
    List<Document> findByUserIdAndCaseIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, UUID caseId);

    /** Trashed documents, most recently trashed first. */
    List<Document> findByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(UUID userId);

    Optional<Document> findByIdAndUserId(UUID id, UUID userId);
}
