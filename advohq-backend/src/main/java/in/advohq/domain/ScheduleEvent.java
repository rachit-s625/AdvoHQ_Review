package in.advohq.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "schedule_events")
@Getter
@Setter
@NoArgsConstructor
public class ScheduleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** hearing | meeting | deadline | filing | other */
    @Column(nullable = false, length = 40)
    private String type = "other";

    @Column(name = "case_name", nullable = false, length = 300)
    private String caseName;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "case_no", length = 120)
    private String caseNo;

    @Column(length = 200)
    private String location;

    @Column(length = 120)
    private String hall;

    @Column(columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
