package in.advohq.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 80)
    private String stage;

    @Column(columnDefinition = "text")
    private String points;

    @Column(name = "case_number", length = 120)
    private String caseNumber;

    @Column(length = 200)
    private String court;

    @Column(name = "assigned_to", length = 120)
    private String assignedTo;

    @Column(length = 120)
    private String hall;

    @Column(name = "linked_event_id")
    private UUID linkedEventId;

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateIso ASC")
    private List<ImportantDate> importantDates = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
