package br.pucminas.diaw.sentinela.service;

import br.pucminas.diaw.sentinela.config.JwtProperties;
import br.pucminas.diaw.sentinela.domain.RefreshToken;
import br.pucminas.diaw.sentinela.domain.User;
import br.pucminas.diaw.sentinela.exception.ApiException;
import br.pucminas.diaw.sentinela.repository.RefreshTokenRepository;
import br.pucminas.diaw.sentinela.security.TokenHasher;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ciclo de vida dos refresh tokens: emissao, rotacao e revogacao.
 * O valor em claro so existe dentro do cookie HttpOnly; o banco guarda o hash.
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository repository, JwtProperties jwtProperties) {
        this.repository = repository;
        this.jwtProperties = jwtProperties;
    }

    /** @return o token em claro, que deve ser devolvido no cookie. */
    @Transactional
    public IssuedToken issue(User user, boolean rememberMe, String userAgent, String ip) {
        Duration ttl = jwtProperties.refreshTtl(rememberMe);
        String rawToken = TokenHasher.randomToken();
        RefreshToken entity = new RefreshToken(
                TokenHasher.sha256(rawToken), user, Instant.now().plus(ttl), truncate(userAgent), ip);
        repository.save(entity);
        return new IssuedToken(rawToken, ttl);
    }

    /** Valida o token recebido, revoga-o e emite outro (rotacao). */
    @Transactional
    public RotationResult rotate(String rawToken, boolean rememberMe, String userAgent, String ip) {
        RefreshToken stored = repository.findByTokenHash(TokenHasher.sha256(rawToken))
                .orElseThrow(() -> ApiException.unauthorized("Sessao expirada. Faca login novamente."));

        if (!stored.isActive()) {
            // Token reaproveitado ou vencido: derruba todas as sessoes do usuario por seguranca.
            repository.revokeAllForUser(stored.getUser(), Instant.now());
            log.warn("Refresh token invalido apresentado para o usuario {}", stored.getUser().getId());
            throw ApiException.unauthorized("Sessao expirada. Faca login novamente.");
        }

        stored.revoke();
        User user = stored.getUser();
        IssuedToken issued = issue(user, rememberMe, userAgent, ip);
        return new RotationResult(user, issued);
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(TokenHasher.sha256(rawToken)).ifPresent(RefreshToken::revoke);
    }

    @Transactional
    public void revokeAllForUser(User user) {
        repository.revokeAllForUser(user, Instant.now());
    }

    /** Remove diariamente os tokens ja vencidos. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        int removed = repository.deleteExpired(Instant.now());
        if (removed > 0) {
            log.info("{} refresh tokens expirados removidos", removed);
        }
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 256 ? value.substring(0, 256) : value;
    }

    public record IssuedToken(String rawToken, Duration ttl) {
    }

    public record RotationResult(User user, IssuedToken token) {
    }
}
