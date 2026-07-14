package in.advohq.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 60) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 20) String phone,
        @Email @Size(max = 160) String email
) {}
