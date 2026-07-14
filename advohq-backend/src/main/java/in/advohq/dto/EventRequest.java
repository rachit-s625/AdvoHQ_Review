package in.advohq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventRequest(
        @Size(max = 40) String type,
        @NotBlank @Size(max = 300) String caseName,
        @NotNull LocalDate date,
        @Size(max = 120) String caseNo,
        @Size(max = 200) String location,
        @Size(max = 120) String hall,
        String notes
) {}
