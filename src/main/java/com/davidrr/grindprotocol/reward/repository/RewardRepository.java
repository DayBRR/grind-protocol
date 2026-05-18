package com.davidrr.grindprotocol.reward.repository;

import com.davidrr.grindprotocol.reward.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByEnabledTrueOrderByCostCorePointsAsc();

    Optional<Reward> findByIdAndEnabledTrue(Long id);
}