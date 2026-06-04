package com.davidrr.grindprotocol.activity.model;

import com.davidrr.grindprotocol.activity.enums.ActivitySourceType;
import com.davidrr.grindprotocol.activity.enums.ActivityType;
import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_activity_events",
        indexes = {
                @Index(name = "idx_user_activity_events_user_profile", columnList = "fk_user_profile"),
                @Index(name = "idx_user_activity_events_type", columnList = "type"),
                @Index(name = "idx_user_activity_events_occurred_at", columnList = "occurred_at"),
                @Index(name = "idx_user_activity_events_source", columnList = "source_type, source_id")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivityEvent extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_user_profile", nullable = false)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private ActivitySourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "xp_delta", nullable = false)
    private Integer xpDelta;

    @Column(name = "core_points_delta", nullable = false)
    private Integer corePointsDelta;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;
}
