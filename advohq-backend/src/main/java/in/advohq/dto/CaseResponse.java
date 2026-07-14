package in.advohq.dto;

import in.advohq.domain.CaseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CaseResponse(
        UUID id,
        String title,
        String stage,
        String points,
        String caseNumber,
        String court,
        String assignedTo,
        String hall,
        UUID linkedEventId,
        List<ImportantDateDto> importantDates,
        Instant createdAt,
        Instant updatedAt
) {
    public static CaseResponse from(CaseEntity c) {
        return new CaseResponse(
                c.getId(), c.getTitle(), c.getStage(), c.getPoints(),
                c.getCaseNumber(), c.getCourt(), c.getAssignedTo(), c.getHall(), c.getLinkedEventId(),
                c.getImportantDates().stream().map(ImportantDateDto::from).toList(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
