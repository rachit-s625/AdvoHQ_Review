package in.advohq.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Metadata for a client-provided document. The bytes themselves are stored in
 * AWS S3 under {@link #s3Key}; only this record lives in PostgreSQL.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Optional link to the case this document belongs to. */
    @Column(name = "case_id")
    private UUID caseId;

    @Column(name = "file_name", nullable = false, length = 300)
    private String fileName;

    @Column(name = "content_type", length = 160)
    private String contentType;

    /** pdf | docx | image | other */
    @Column(nullable = false, length = 20)
    private String kind = "other";

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes = 0;

    @Column(name = "s3_key", nullable = false, unique = true, length = 600)
    private String s3Key;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Non-null once moved to Trash; null while active in the Library. */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
