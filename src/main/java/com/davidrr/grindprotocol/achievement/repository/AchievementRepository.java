package com.davidrr.grindprotocol.achievement.repository;

import com.davidrr.grindprotocol.achievement.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository
        extends JpaRepository<Achievement, Long> {

    Optional<Achievement> findByCode(String code);

    List<Achievement> findByEnabledTrueOrderByIdAsc();
}