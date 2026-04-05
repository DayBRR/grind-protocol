package com.davidrr.grindprotocol.task.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.task.enums.CompletionSource;
import com.davidrr.grindprotocol.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "task_completions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_completions_task_date_index",
                        columnNames = {"task_id", "completion_date", "completion_index_for_day"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompletion extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tarea completada.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * Usuario que realiza el completado.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    /**
     * Fecha de negocio usada para controlar repeticiones y cierre diario.
     */
    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    /**
     * Índice del completion dentro del mismo día.
     * Ej: 1, 2, 3...
     */
    @Column(name = "completion_index_for_day", nullable = false)
    private Integer completionIndexForDay;

    @Column(name = "counted_for_daily_goal", nullable = false)
    private boolean countedForDailyGoal;

    @Column(name = "counted_for_streak", nullable = false)
    private boolean countedForStreak;

    @Column(name = "base_xp", nullable = false)
    private Integer baseXp;

    @Column(name = "awarded_xp", nullable = false)
    private Integer awardedXp;

    @Column(name = "awarded_core_points", nullable = false)
    private Integer awardedCorePoints;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompletionSource source;
}