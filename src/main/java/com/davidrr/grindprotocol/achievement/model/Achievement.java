package com.davidrr.grindprotocol.achievement.model;

import com.davidrr.grindprotocol.achievement.enums.AchievementType;
import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "achievements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_achievements_code",
                        columnNames = "code"
                )
        }
)
public class Achievement extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AchievementType type;

    @Column(name = "target_value", nullable = false)
    private Long targetValue;

    @Column(name = "xp_reward", nullable = false)
    private Long xpReward = 0L;

    @Column(name = "core_points_reward", nullable = false)
    private Long corePointsReward = 0L;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "hidden", nullable = false)
    private Boolean hidden = false;
}