package com.davidrr.grindprotocol.userprofile.repository;

import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    // Acquire before loading or changing claim instances. Held until transaction completion.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserProfile> findLockedByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("select p.user.id from UserProfile p order by p.user.id")
    List<Long> findUserIds(Pageable page);
}
