package in.advohq.web;

import in.advohq.dto.*;
import in.advohq.security.AppUserPrincipal;
import in.advohq.security.CurrentUser;
import in.advohq.service.AccountService;
import in.advohq.service.AuthService;
import in.advohq.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Authenticated endpoints for the signed-in user's profile, key/value settings,
 * and custom case stages.
 */
@RestController
@RequestMapping("/api")
public class ProfileController {

    private final SettingsService settings;
    private final AuthService auth;
    private final AccountService account;

    public ProfileController(SettingsService settings, AuthService auth, AccountService account) {
        this.settings = settings;
        this.auth = auth;
        this.account = account;
    }

    // ── profile ──────────────────────────────────────────────────────
    @GetMapping("/me")
    public UserResponse me(@CurrentUser AppUserPrincipal me) {
        return settings.getProfile(me.getId());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@CurrentUser AppUserPrincipal me,
                                 @Valid @RequestBody ProfileUpdateRequest req) {
        return settings.updateProfile(me.getId(), req);
    }

    /** Returns a fresh token: changing the password invalidates all older ones. */
    @PutMapping("/me/password")
    public AuthResponse changePassword(@CurrentUser AppUserPrincipal me,
                                       @Valid @RequestBody ChangePasswordRequest req) {
        return auth.changePassword(me.getId(), req);
    }

    /** Permanently deletes the account, all its data, and its files in S3. */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@CurrentUser AppUserPrincipal me) {
        account.deleteAccount(me.getId());
    }

    // ── key/value settings ───────────────────────────────────────────
    @GetMapping("/settings")
    public List<SettingDto> settings(@CurrentUser AppUserPrincipal me) {
        return settings.listSettings(me.getId());
    }

    @PutMapping("/settings")
    public SettingDto upsertSetting(@CurrentUser AppUserPrincipal me,
                                    @Valid @RequestBody SettingDto dto) {
        return settings.upsertSetting(me.getId(), dto);
    }

    // ── custom case stages ───────────────────────────────────────────
    @GetMapping("/stages")
    public List<CustomStageDto> stages(@CurrentUser AppUserPrincipal me) {
        return settings.listStages(me.getId());
    }

    @PostMapping("/stages")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomStageDto addStage(@CurrentUser AppUserPrincipal me,
                                   @Valid @RequestBody CustomStageDto dto) {
        return settings.addStage(me.getId(), dto);
    }

    @DeleteMapping("/stages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStage(@CurrentUser AppUserPrincipal me, @PathVariable UUID id) {
        settings.deleteStage(me.getId(), id);
    }
}
