package in.advohq.dto;

import in.advohq.domain.UserSetting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SettingDto(
        @NotBlank @Size(max = 80) String key,
        @NotBlank @Size(max = 500) String value
) {
    public static SettingDto from(UserSetting s) {
        return new SettingDto(s.getSettingKey(), s.getSettingValue());
    }
}
