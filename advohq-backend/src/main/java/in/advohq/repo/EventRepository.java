package in.advohq.repo;

import in.advohq.domain.ScheduleEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<ScheduleEvent, UUID> {

    List<ScheduleEvent> findByUserIdOrderByEventDateAsc(UUID userId);

    List<ScheduleEvent> findByUserIdAndEventDateBetweenOrderByEventDateAsc(
            UUID userId, LocalDate from, LocalDate to);

    Optional<ScheduleEvent> findByIdAndUserId(UUID id, UUID userId);
}
