package com.davidrr.grindprotocol.activity.repository;

import com.davidrr.grindprotocol.activity.model.UserActivityEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityEventRepository extends JpaRepository<UserActivityEvent, Long> {

    List<UserActivityEvent> findByUserProfileUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable);

    List<UserActivityEvent> findByUserProfileUserIdAndOccurredAtBetweenOrderByOccurredAtAsc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
