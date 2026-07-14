package in.advohq.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CaseRequest(
        @NotBlank @Size(max = 300) String title,
        @Size(max = 80) String stage,
        String points,
        @Size(max = 120) String caseNumber,
        @Size(max = 200) String court,
        @Size(max = 120) String assignedTo,
        @Size(max = 120) String hall,
        UUID linkedEventId,
        @Valid List<ImportantDateDto> importantDates
) {}
