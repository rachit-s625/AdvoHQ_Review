package in.advohq.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A single key/value preference for a user (e.g. {@code notifications -> "1"},
 * mirroring the front-end's {@code advohq_<key>} localStorage flags).
 */
@Entity
@Table(name = "user_settings",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_setting", columnNames = {"user_id", "setting_key"}))
@Getter
@Setter
@NoArgsConstructor
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "setting_key", nullable = false, length = 80)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, length = 500)
    private String settingValue;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
