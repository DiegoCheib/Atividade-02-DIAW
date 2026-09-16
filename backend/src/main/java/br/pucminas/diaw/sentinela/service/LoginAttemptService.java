package br.pucminas.diaw.sentinela.service;

import br.pucminas.diaw.sentinela.config.SecurityPolicyProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Bloqueio temporario apos varias tentativas de login malsucedidas.
 * Mantido em memoria por simplicidade; em producao usaria Redis ou banco.
 */
@Service
public class LoginAttemptService {

    private record Attempt(AtomicInteger count, Instant lockedUntil) {
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final SecurityPolicyProperties properties;

    public LoginAttemptService(SecurityPolicyProperties properties) {
        this.properties = properties;
    }

    private static String key(String identifier, String ip) {
        return (identifier == null ? "" : identifier.toLowerCase()) + "|" + (ip == null ? "" : ip);
    }

    /** @return tempo restante de bloqueio, ou {@link Duration#ZERO} se liberado. */
    public Duration remainingLock(String identifier, String ip) {
        Attempt attempt = attempts.get(key(identifier, ip));
        if (attempt == null || attempt.lockedUntil() == null) {
            return Duration.ZERO;
        }
        Duration remaining = Duration.between(Instant.now(), attempt.lockedUntil());
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    public void registerFailure(String identifier, String ip) {
        String cacheKey = key(identifier, ip);
        attempts.compute(cacheKey, (k, current) -> {
            if (current == null || (current.lockedUntil() != null && current.lockedUntil().isBefore(Instant.now()))) {
                return new Attempt(new AtomicInteger(1), null);
            }
            int total = current.count().incrementAndGet();
            if (total >= properties.loginMaxAttempts()) {
                return new Attempt(current.count(), Instant.now().plus(properties.loginLockDuration()));
            }
            return current;
        });
    }

    public int remainingAttempts(String identifier, String ip) {
        Attempt attempt = attempts.get(key(identifier, ip));
        if (attempt == null) {
            return properties.loginMaxAttempts();
        }
        return Math.max(0, properties.loginMaxAttempts() - attempt.count().get());
    }

    public void registerSuccess(String identifier, String ip) {
        attempts.remove(key(identifier, ip));
    }

    /** Limpa registros antigos a cada hora para o mapa nao crescer indefinidamente. */
    @Scheduled(fixedRate = 3_600_000L)
    public void purgeExpired() {
        Instant now = Instant.now();
        attempts.entrySet().removeIf(entry -> {
            Instant lockedUntil = entry.getValue().lockedUntil();
            return lockedUntil != null && lockedUntil.isBefore(now);
        });
    }
}
