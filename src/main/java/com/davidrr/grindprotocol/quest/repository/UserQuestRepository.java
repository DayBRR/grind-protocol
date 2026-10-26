package com.davidrr.grindprotocol.quest.repository;

import com.davidrr.grindprotocol.quest.model.UserQuest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.Optional;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    // No entity graph: lock the instance, without locking shared quest definitions.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserQuest> findLockedByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
            Long userId, Long questId, LocalDate periodStart, LocalDate periodEnd);

    @EntityGraph(attributePaths = {"quest", "userProfile"})
    Optional<UserQuest> findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
            Long userId,
            Long questId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

}
