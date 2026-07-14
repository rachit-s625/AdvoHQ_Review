package in.advohq.dto;

import in.advohq.domain.ImportantDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/** Used both as request (id may be null on create) and response payload. */
public record ImportantDateDto(
        UUID id,
        @NotNull LocalDate dateISO,
        @NotBlank String label,
        boolean notified
) {
    public static ImportantDateDto from(ImportantDate d) {
        return new ImportantDateDto(d.getId(), d.getDateIso(), d.getLabel(), d.isNotified());
    }
}
