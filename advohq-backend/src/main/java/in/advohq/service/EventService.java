package in.advohq.service;

import in.advohq.domain.ScheduleEvent;
import in.advohq.dto.EventRequest;
import in.advohq.dto.EventResponse;
import in.advohq.exception.NotFoundException;
import in.advohq.repo.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EventService {

    private static final Set<String> VALID_TYPES =
            Set.of("hearing", "meeting", "deadline", "filing", "other");

    private final EventRepository events;

    public EventService(EventRepository events) {
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list(UUID userId, LocalDate from, LocalDate to) {
        List<ScheduleEvent> found = (from != null && to != null)
                ? events.findByUserIdAndEventDateBetweenOrderByEventDateAsc(userId, from, to)
                : events.findByUserIdOrderByEventDateAsc(userId);
        return found.stream().map(EventResponse::from).toList();
    }

    @Transactional
    public EventResponse create(UUID userId, EventRequest req) {
        ScheduleEvent e = new ScheduleEvent();
        e.setUserId(userId);
        apply(e, req);
        return EventResponse.from(events.save(e));
    }

    @Transactional
    public EventResponse update(UUID userId, UUID id, EventRequest req) {
        ScheduleEvent e = events.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        apply(e, req);
        return EventResponse.from(events.save(e));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        ScheduleEvent e = events.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        events.delete(e);
    }

    private void apply(ScheduleEvent e, EventRequest req) {
        String type = StringUtils.hasText(req.type()) ? req.type().toLowerCase() : "other";
        e.setType(VALID_TYPES.contains(type) ? type : "other");
        e.setCaseName(req.caseName().trim());
        e.setEventDate(req.date());
        e.setCaseNo(req.caseNo());
        e.setLocation(req.location());
        e.setHall(req.hall());
        e.setNotes(req.notes());
    }
}
