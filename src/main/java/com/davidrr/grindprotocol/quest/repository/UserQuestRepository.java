package com.davidrr.grindprotocol.quest.repository;

import com.davidrr.grindprotocol.quest.model.UserQuest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    @EntityGraph(attributePaths = {"quest", "userProfile"})
    Optional<UserQuest> findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
            Long userId,
            Long questId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

}