package in.advohq.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "important_dates")
@Getter
@Setter
@NoArgsConstructor
public class ImportantDate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseEntity;

    @Column(name = "date_iso", nullable = false)
    private LocalDate dateIso;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false)
    private boolean notified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
