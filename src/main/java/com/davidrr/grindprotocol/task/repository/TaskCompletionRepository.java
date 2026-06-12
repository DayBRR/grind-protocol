package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.enums.TaskCategory;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    long countByUserIdAndCompletionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
            select t.category as category,
                   count(tc.id) as completedTasks,
                   coalesce(sum(tc.awardedXp), 0) as xpEarned,
                   coalesce(sum(tc.awardedCorePoints), 0) as corePointsEarned
            from TaskCompletion tc
            join tc.task t
            where tc.user.id = :userId
              and tc.completionDate >= :startDate
              and tc.completionDate <= :endDate
            group by t.category
            """)
    List<CategoryFocusAggregate> aggregateCategoryFocus(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select t.category as category,
                   count(tc.id) as completedTasks,
                   coalesce(sum(tc.awardedXp), 0) as xpEarned,
                   coalesce(sum(tc.awardedCorePoints), 0) as corePointsEarned
            from TaskCompletion tc
            join tc.task t
            where tc.user.id = :userId
            group by t.category
            """)
    List<CategoryFocusAggregate> aggregateCategoryFocusAllTime(
            @Param("userId") Long userId
    );

    interface CategoryFocusAggregate {

        TaskCategory getCategory();

        Long getCompletedTasks();

        Long getXpEarned();

        Long getCorePointsEarned();
    }
}