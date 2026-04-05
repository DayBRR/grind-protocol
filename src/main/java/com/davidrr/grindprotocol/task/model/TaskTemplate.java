package com.davidrr.grindprotocol.task.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.task.enums.TaskCategory;
import com.davidrr.grindprotocol.task.enums.TaskDifficulty;
import com.davidrr.grindprotocol.task.enums.TaskType;
import com.davidrr.grindprotocol.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTemplate extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario creador de la plantilla.
     * Puede ser null si en el futuro quieres plantillas del sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id")
    private User creatorUser;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 50)
    private TaskType taskType;

    @Column(name = "base_xp", nullable = false)
    private Integer baseXp;

    @Column(nullable = false)
    private boolean mandatory;

    @Column(name = "streak_eligible", nullable = false)
    private boolean streakEligible;

    @Column(nullable = false)
    private boolean repeatable;

    @Column(name = "max_completions_per_day", nullable = false)
    private Integer maxCompletionsPerDay;

    @Column(name = "diminishing_returns_enabled", nullable = false)
    private boolean diminishingReturnsEnabled;

    @Column(name = "due_time")
    private LocalTime dueTime;

    /**
     * 1 = Monday ... 7 = Sunday
     */
    @Column(name = "weekly_closing_day")
    private Integer weeklyClosingDay;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private boolean active;

    /**
     * Rasgos RPG asociados a esta plantilla.
     * Ejemplo: FOCUS + DISCIPLINE + TECH_SKILL.
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "task_template_traits",
            joinColumns = @JoinColumn(name = "task_template_id"),
            inverseJoinColumns = @JoinColumn(name = "trait_id")
    )
    private Set<Trait> traits = new HashSet<>();
}