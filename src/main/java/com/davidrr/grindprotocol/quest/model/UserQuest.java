package com.davidrr.grindprotocol.quest.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "user_quests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_quest_period",
                        columnNames = {
                                "fk_user_profile",
                                "fk_quest",
                                "period_start",
                                "period_end"
                        }
                )
        }
)
public class UserQuest extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_user_profile", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_quest", nullable = false)
    private Quest quest;

    @Column(name = "progress_value", nullable = false)
    private Long progressValue = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestStatus status = QuestStatus.ACTIVE;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;
}