package com.davidrr.grindprotocol.user.adapter;

import com.davidrr.grindprotocol.user.model.RefreshToken;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.RefreshTokenRepository;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.security.auth.session.model.RefreshSession;
import com.davidrr.security.auth.session.port.RefreshSessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaRefreshSessionStore implements RefreshSessionStore {

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;

    @Override
    public RefreshSession save(RefreshSession session) {
        RefreshToken entity = toManagedEntity(session);
        RefreshToken saved = repository.save(entity);
        return mapToModel(saved);
    }

    @Override
    public List<RefreshSession> saveAll(List<RefreshSession> sessions) {
        List<RefreshToken> entities = sessions.stream()
                .map(this::toManagedEntity)
                .toList();

        return repository.saveAll(entities).stream()
                .map(this::mapToModel)
                .toList();
    }

    @Override
    public Optional<RefreshSession> findById(Long id) {
        return repository.findById(id).map(this::mapToModel);
    }

    @Override
    public Optional<RefreshSession> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::mapToModel);
    }

    @Override
    public List<RefreshSession> findActiveByUsername(String username) {
        return repository.findByUserUsernameAndRevokedFalse(username).stream()
                .map(this::mapToModel)
                .toList();
    }

    @Override
    public List<RefreshSession> findActiveByUsernameOrderByCreatedAtDesc(String username) {
        return repository.findByUserUsernameAndRevokedFalseOrderByCreatedAtDesc(username).stream()
                .map(this::mapToModel)
                .toList();
    }

    @Override
    public List<RefreshSession> findExpiredActiveSessions(Instant now) {
        return repository.findByExpiresAtBeforeAndRevokedFalse(now).stream()
                .map(this::mapToModel)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private RefreshSession mapToModel(RefreshToken entity) {
        return RefreshSession.builder()
                .id(entity.getId())
                .username(entity.getUser().getUsername())
                .tokenHash(entity.getTokenHash())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .lastUsedAt(entity.getLastUsedAt())
                .revoked(entity.isRevoked())
                .revokedAt(entity.getRevokedAt())
                .revocationReason(entity.getRevocationReason())
                .replacedByTokenHash(entity.getReplacedByTokenHash())
                .ip(entity.getIp())
                .userAgent(entity.getUserAgent())
                .build();
    }

    private RefreshToken toManagedEntity(RefreshSession session) {
        RefreshToken entity = session.getId() != null
                ? repository.findById(session.getId()).orElseGet(RefreshToken::new)
                : new RefreshToken();

        if (entity.getUser() == null) {
            User managedUser = userRepository.findByUsername(session.getUsername())
                    .orElseThrow(() -> new IllegalStateException(
                            "User not found for refresh session username: " + session.getUsername()
                    ));
            entity.setUser(managedUser);
        }

        entity.setTokenHash(session.getTokenHash());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setExpiresAt(session.getExpiresAt());
        entity.setLastUsedAt(session.getLastUsedAt());
        entity.setRevoked(session.isRevoked());
        entity.setRevokedAt(session.getRevokedAt());
        entity.setRevocationReason(session.getRevocationReason());
        entity.setReplacedByTokenHash(session.getReplacedByTokenHash());
        entity.setIp(session.getIp());
        entity.setUserAgent(session.getUserAgent());

        return entity;
    }
}