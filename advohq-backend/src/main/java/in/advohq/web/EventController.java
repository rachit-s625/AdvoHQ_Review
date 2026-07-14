package in.advohq.web;

import in.advohq.dto.EventRequest;
import in.advohq.dto.EventResponse;
import in.advohq.security.AppUserPrincipal;
import in.advohq.security.CurrentUser;
import in.advohq.service.EventService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService events;

    public EventController(EventService events) {
        this.events = events;
    }

    /** Optionally filter by an inclusive [from,to] date range (e.g. one calendar month). */
    @GetMapping
    public List<EventResponse> list(
            @CurrentUser AppUserPrincipal me,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return events.list(me.getId(), from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@CurrentUser AppUserPrincipal me, @Valid @RequestBody EventRequest req) {
        return events.create(me.getId(), req);
    }

    @PutMapping("/{id}")
    public EventResponse update(@CurrentUser AppUserPrincipal me,
                                @PathVariable UUID id,
                                @Valid @RequestBody EventRequest req) {
        return events.update(me.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        events.delete(me.getId(), id);
    }
}
