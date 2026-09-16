package br.pucminas.diaw.sentinela.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Contas de demonstracao criadas no primeiro start, para facilitar a avaliacao.
 * Desative com app.demo-user.enabled=false antes de publicar a aplicacao.
 */
@ConfigurationProperties(prefix = "app.demo-user")
public record DemoUserProperties(

        @DefaultValue("true") boolean enabled,
        @DefaultValue("Usuario Demonstracao") String name,
        @DefaultValue("demo") String username,
        @DefaultValue("demo@pucminas.br") String email,
        @DefaultValue("Demo@1234") String password,
        @DefaultValue("Administrador") String adminName,
        @DefaultValue("admin") String adminUsername,
        @DefaultValue("admin@pucminas.br") String adminEmail,
        @DefaultValue("Admin@1234") String adminPassword
) {
}
