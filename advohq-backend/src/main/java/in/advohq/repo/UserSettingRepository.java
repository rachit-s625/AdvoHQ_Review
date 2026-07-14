package in.advohq.repo;

import in.advohq.domain.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSettingRepository extends JpaRepository<UserSetting, UUID> {
    List<UserSetting> findByUserId(UUID userId);
    Optional<UserSetting> findByUserIdAndSettingKey(UUID userId, String settingKey);
}
