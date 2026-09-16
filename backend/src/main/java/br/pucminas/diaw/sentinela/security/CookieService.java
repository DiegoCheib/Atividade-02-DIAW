package br.pucminas.diaw.sentinela.security;

import br.pucminas.diaw.sentinela.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cookie HttpOnly do refresh token. Fora do alcance do JavaScript, o que
 * protege a sessao contra XSS; SameSite evita envio em requisicoes de terceiros.
 */
@Component
public class CookieService {

    private final JwtProperties properties;

    public CookieService(JwtProperties properties) {
        this.properties = properties;
    }

    public String cookieName() {
        return properties.cookieName();
    }

    public String buildSetCookieHeader(String rawToken, Duration ttl) {
        return ResponseCookie.from(properties.cookieName(), rawToken)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .path(properties.cookiePath())
                .maxAge(ttl)
                .sameSite(properties.cookieSameSite())
                .build()
                .toString();
    }

    public String buildClearCookieHeader() {
        return ResponseCookie.from(properties.cookieName(), "")
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .path(properties.cookiePath())
                .maxAge(Duration.ZERO)
                .sameSite(properties.cookieSameSite())
                .build()
                .toString();
    }

    public Optional<String> readRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> properties.cookieName().equals(cookie.getName()))
                .map(cookie -> cookie.getValue())
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    public String header() {
        return HttpHeaders.SET_COOKIE;
    }
}
