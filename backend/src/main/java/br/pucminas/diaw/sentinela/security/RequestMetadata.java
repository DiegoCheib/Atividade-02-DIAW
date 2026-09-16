package br.pucminas.diaw.sentinela.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

/** Dados do cliente usados para auditoria da sessao e para o controle de tentativas. */
public record RequestMetadata(String ip, String userAgent) {

    public static RequestMetadata from(HttpServletRequest request) {
        return new RequestMetadata(clientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
