package com.davidrr.grindprotocol.reward.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.reward.enums.RewardCategory;
import com.davidrr.grindprotocol.reward.enums.RewardType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rewards")
public class Reward extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RewardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RewardCategory category;

    @Column(name = "cost_core_points", nullable = false)
    private Long costCorePoints;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean repeatable = true;

    @Column(name = "cooldown_days")
    private Integer cooldownDays;

    @Column(name = "required_level")
    private Long requiredLevel;

    @Column(name = "required_current_streak")
    private Long requiredCurrentStreak;
}