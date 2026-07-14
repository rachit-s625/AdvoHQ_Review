package in.advohq.service;

import in.advohq.domain.CustomStage;
import in.advohq.domain.User;
import in.advohq.domain.UserSetting;
import in.advohq.dto.*;
import in.advohq.exception.NotFoundException;
import in.advohq.repo.CustomStageRepository;
import in.advohq.repo.UserRepository;
import in.advohq.repo.UserSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class SettingsService {

    private final UserSettingRepository settings;
    private final CustomStageRepository stages;
    private final UserRepository users;

    public SettingsService(UserSettingRepository settings, CustomStageRepository stages, UserRepository users) {
        this.settings = settings;
        this.stages = stages;
        this.users = users;
    }

    // ── key/value preferences ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SettingDto> listSettings(UUID userId) {
        return settings.findByUserId(userId).stream().map(SettingDto::from).toList();
    }

    @Transactional
    public SettingDto upsertSetting(UUID userId, SettingDto dto) {
        UserSetting s = settings.findByUserIdAndSettingKey(userId, dto.key())
                .orElseGet(() -> {
                    UserSetting ns = new UserSetting();
                    ns.setUserId(userId);
                    ns.setSettingKey(dto.key());
                    return ns;
                });
        s.setSettingValue(dto.value());
        return SettingDto.from(settings.save(s));
    }

    // ── profile ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return UserResponse.from(requireUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, ProfileUpdateRequest req) {
        User u = requireUser(userId);
        if (StringUtils.hasText(req.fullName()))    u.setFullName(req.fullName().trim());
        if (req.displayName() != null)              u.setDisplayName(req.displayName().trim());
        if (req.phone() != null)                    u.setPhone(req.phone());
        if (req.email() != null)                    u.setEmail(req.email());
        if (req.twoFactorEnabled() != null)         u.setTwoFactorEnabled(req.twoFactorEnabled());
        return UserResponse.from(users.save(u));
    }

    // ── custom case stages ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CustomStageDto> listStages(UUID userId) {
        return stages.findByUserIdOrderBySortOrderAsc(userId).stream().map(CustomStageDto::from).toList();
    }

    @Transactional
    public CustomStageDto addStage(UUID userId, CustomStageDto dto) {
        CustomStage s = new CustomStage();
        s.setUserId(userId);
        s.setName(dto.name().trim());
        s.setSortOrder(dto.sortOrder());
        return CustomStageDto.from(stages.save(s));
    }

    @Transactional
    public void deleteStage(UUID userId, UUID stageId) {
        CustomStage s = stages.findByIdAndUserId(stageId, userId)
                .orElseThrow(() -> new NotFoundException("Stage not found"));
        stages.delete(s);
    }

    private User requireUser(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }
}
