package com.davidrr.grindprotocol.quest.repository;

import com.davidrr.grindprotocol.quest.model.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    Optional<Quest> findByCode(String code);

    List<Quest> findByEnabledTrueOrderByIdAsc();
}