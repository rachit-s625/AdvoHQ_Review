package in.advohq.dto;

import in.advohq.domain.ScheduleEvent;

import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String type,
        String caseName,
        LocalDate date,
        String caseNo,
        String location,
        String hall,
        String notes
) {
    public static EventResponse from(ScheduleEvent e) {
        return new EventResponse(
                e.getId(), e.getType(), e.getCaseName(), e.getEventDate(),
                e.getCaseNo(), e.getLocation(), e.getHall(), e.getNotes());
    }
}
