package com.davidrr.grindprotocol.achievement.repository;

import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository
        extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement>
    findByUserProfileUserIdOrderByUnlockedAtDesc(Long userId);

    Optional<UserAchievement>
    findByUserProfileUserIdAndAchievementId(
            Long userId,
            Long achievementId
    );

    boolean existsByUserProfileUserIdAndAchievementId(
            Long userId,
            Long achievementId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserAchievement> findLockedByUserProfileUserIdAndAchievementId(Long userId, Long achievementId);
}
