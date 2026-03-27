package com.davidrr.grindprotocol.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_hash", columnList = "tokenHash", unique = true),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Guardamos SOLO el hash del refresh token (nunca el token en claro).
     * Así si se filtra la BD, no pueden usarlo tal cual.
     */
    @Column(nullable = false, unique = true, length = 64) // SHA-256 hex = 64 chars
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private Instant revokedAt;

    @Column(length = 50)
    private String revocationReason;

    @Column
    private Instant lastUsedAt;

    @Column(length = 64)
    private String replacedByTokenHash;

    @Column(length = 128)
    private String ip;

    @Column(length = 512)
    private String userAgent;

    @PrePersist
    void onCreate() {
        if (createdAt == null) { createdAt = Instant.now(); }
        if (!revoked) { revoked = false; }
    }
}
