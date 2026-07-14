package in.advohq.dto;

import in.advohq.domain.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String fullName,
        String displayName,
        String phone,
        String email,
        boolean twoFactorEnabled
) {
    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(), u.getUsername(), u.getFullName(),
                u.getDisplayName(), u.getPhone(), u.getEmail(), u.isTwoFactorEnabled());
    }
}
