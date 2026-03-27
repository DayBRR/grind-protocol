package com.davidrr.grindprotocol.user.repository;

import com.davidrr.grindprotocol.user.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    List<RefreshToken> findByExpiresAtBeforeAndRevokedFalse(Instant now);

    List<RefreshToken> findByUserUsernameAndRevokedFalse(String username);

    List<RefreshToken> findByUserUsernameAndRevokedFalseOrderByCreatedAtDesc(String username);

}
