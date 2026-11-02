package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.model.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface DailyProgressRepository extends JpaRepository<DailyProgress, Long> {

    Optional<DailyProgress> findByUserIdAndProgressDate(Long userId, LocalDate progressDate);

    List<DailyProgress> findByUserIdAndProgressDateBetweenOrderByProgressDateAsc(
            Long userId, LocalDate start, LocalDate end);
}
