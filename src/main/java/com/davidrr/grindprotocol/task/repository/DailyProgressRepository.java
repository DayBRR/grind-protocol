package com.davidrr.grindprotocol.task.repository;

import com.davidrr.grindprotocol.task.model.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyProgressRepository extends JpaRepository<DailyProgress, Long> {

    Optional<DailyProgress> findByUserIdAndProgressDate(Long userId, LocalDate progressDate);
}