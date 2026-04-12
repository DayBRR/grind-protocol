package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.model.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {

    List<TaskCompletion> findByUserIdAndCompletionDate(Long userId, LocalDate completionDate);

    List<TaskCompletion> findByTaskIdAndCompletionDate(Long taskId, LocalDate completionDate);

    Optional<TaskCompletion> findTopByTaskIdAndCompletionDateOrderByCompletionIndexForDayDesc(
            Long taskId,
            LocalDate completionDate
    );

    long countByUserIdAndCompletionDate(Long userId, LocalDate completionDate);
}