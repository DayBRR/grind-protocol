package com.davidrr.grindprotocol.quest.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.quest.enums.QuestFrequency;
import com.davidrr.grindprotocol.quest.enums.QuestType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "quests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_quests_code",
                        columnNames = "code"
                )
        }
)
public class Quest extends BaseAuditableEntity {

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
    private QuestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestFrequency frequency;

    @Column(name = "target_value", nullable = false)
    private Long targetValue;

    @Column(name = "xp_reward", nullable = false)
    private Long xpReward = 0L;

    @Column(name = "core_points_reward", nullable = false)
    private Long corePointsReward = 0L;

    @Column(nullable = false)
    private Boolean enabled = true;
}