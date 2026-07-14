package in.advohq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Partial update of the signed-in user's profile; null fields are left unchanged. */
public record ProfileUpdateRequest(
        @Size(max = 120) String fullName,
        @Size(max = 120) String displayName,
        @Size(max = 20) String phone,
        @Email @Size(max = 160) String email,
        Boolean twoFactorEnabled
) {}
