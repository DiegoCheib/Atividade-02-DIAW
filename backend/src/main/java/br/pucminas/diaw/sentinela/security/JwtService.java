package br.pucminas.diaw.sentinela.security;

import br.pucminas.diaw.sentinela.config.JwtProperties;
import br.pucminas.diaw.sentinela.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Emissao e validacao do access token (JWT assinado com HMAC-SHA256). */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final int MIN_SECRET_BYTES = 32;

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = resolveKey(properties.secret());
    }

    private static SecretKey resolveKey(String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("app.jwt.secret nao definido: uma chave aleatoria foi gerada para esta execucao. "
                    + "Os tokens deixam de valer a cada reinicio. Defina a variavel JWT_SECRET em producao.");
            return Jwts.SIG.HS256.key().build();
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret precisa ter no minimo " + MIN_SECRET_BYTES + " caracteres");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(properties.accessTokenTtl());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    /** @return as claims do token ou {@code null} se ele for invalido/expirado. */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT rejeitado: {}", e.getMessage());
            return null;
        }
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }
}
