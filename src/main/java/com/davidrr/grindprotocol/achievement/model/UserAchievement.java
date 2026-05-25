package com.davidrr.grindprotocol.achievement.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_achievement",
                        columnNames = {
                                "fk_user_profile",
                                "fk_achievement"
                        }
                )
        }
)
public class UserAchievement extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_user_profile", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_achievement", nullable = false)
    private Achievement achievement;

    @Column(name = "progress_value", nullable = false)
    private Long progressValue = 0L;

    @Column(name = "unlocked", nullable = false)
    private Boolean unlocked = false;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "claimed", nullable = false)
    private Boolean claimed = false;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;
}