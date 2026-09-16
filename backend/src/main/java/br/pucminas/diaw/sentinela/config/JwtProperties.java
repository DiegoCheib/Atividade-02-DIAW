package br.pucminas.diaw.sentinela.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracoes do JWT e do cookie que transporta o refresh token.
 * O segredo deve vir de variavel de ambiente (JWT_SECRET) em producao.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        @DefaultValue("") String secret,
        @DefaultValue("sentinela-diaw") String issuer,
        @DefaultValue("15m") Duration accessTokenTtl,
        @DefaultValue("7d") Duration refreshTokenTtl,
        @DefaultValue("30d") Duration refreshTokenRememberTtl,
        @DefaultValue("sentinela_refresh") String cookieName,
        @DefaultValue("/api/auth") String cookiePath,
        @DefaultValue("false") boolean cookieSecure,
        @DefaultValue("Lax") String cookieSameSite
) {

    public Duration refreshTtl(boolean rememberMe) {
        return rememberMe ? refreshTokenRememberTtl : refreshTokenTtl;
    }
}
