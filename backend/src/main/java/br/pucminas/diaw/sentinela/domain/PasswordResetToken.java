package br.pucminas.diaw.sentinela.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Token de redefinicao de senha enviado por e-mail.
 *
 * <p>Assim como o refresh token, apenas o hash SHA-256 e persistido: o valor em
 * claro existe somente no link que chega ao usuario. O token e de uso unico e
 * vence no prazo definido em {@code app.mail.reset-token-ttl}.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "requested_ip", length = 60)
    private String requestedIp;

    protected PasswordResetToken() {
        // exigido pelo JPA
    }

    public PasswordResetToken(String tokenHash, User user, Instant expiresAt, String requestedIp) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
        this.requestedIp = requestedIp;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isUsable() {
        return usedAt == null && expiresAt.isAfter(Instant.now());
    }

    public void markUsed() {
        if (usedAt == null) {
            this.usedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public User getUser() {
        return user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getRequestedIp() {
        return requestedIp;
    }
}
