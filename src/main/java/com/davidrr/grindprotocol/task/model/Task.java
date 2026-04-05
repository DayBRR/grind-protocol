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
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario propietario de la tarea.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Plantilla usada para crear la tarea.
     * Nullable si la tarea es personalizada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private TaskTemplate template;

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

    @Column(nullable = false)
    private boolean active;

    @Column(name = "due_time")
    private LocalTime dueTime;

    /**
     * 1 = Monday ... 7 = Sunday
     */
    @Column(name = "weekly_closing_day")
    private Integer weeklyClosingDay;

    /**
     * Rasgos RPG asociados a esta tarea.
     * Ejemplo: DISCIPLINE + TECH_SKILL.
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "task_traits",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "trait_id")
    )
    private Set<Trait> traits = new HashSet<>();
}