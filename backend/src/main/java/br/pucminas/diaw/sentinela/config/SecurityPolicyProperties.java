package br.pucminas.diaw.sentinela.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Politicas de acesso: origens liberadas no CORS e bloqueio por tentativas de login. */
@ConfigurationProperties(prefix = "app.security")
public record SecurityPolicyProperties(

        @DefaultValue({"http://localhost:5173", "http://localhost:4173"}) List<String> allowedOrigins,
        @DefaultValue("5") int loginMaxAttempts,
        @DefaultValue("15m") Duration loginLockDuration
) {
}
