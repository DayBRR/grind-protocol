package com.davidrr.grindprotocol.task.model;

import com.davidrr.grindprotocol.common.model.BaseAuditableEntity;
import com.davidrr.grindprotocol.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_progress_user_date",
                        columnNames = {"user_id", "progress_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyProgress extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario al que pertenece el progreso diario.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "progress_date", nullable = false)
    private LocalDate progressDate;

    @Column(name = "required_task_count", nullable = false)
    private Integer requiredTaskCount;

    @Column(name = "completed_valid_task_count", nullable = false)
    private Integer completedValidTaskCount;

    @Column(name = "mandatory_tasks_required", nullable = false)
    private Integer mandatoryTasksRequired;

    @Column(name = "mandatory_tasks_completed", nullable = false)
    private Integer mandatoryTasksCompleted;

    @Column(name = "day_qualified", nullable = false)
    private boolean dayQualified;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;
}