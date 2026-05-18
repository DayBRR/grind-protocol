package com.davidrr.grindprotocol.reward.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reward_redemptions")
public class RewardRedemption extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_reward", nullable = false)
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_user_profile", nullable = false)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RewardRedemptionStatus status;

    @Column(name = "cost_paid", nullable = false)
    private Long costPaid;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(length = 1000)
    private String notes;
}