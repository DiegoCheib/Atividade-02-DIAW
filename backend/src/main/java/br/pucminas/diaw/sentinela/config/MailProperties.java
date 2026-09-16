package br.pucminas.diaw.sentinela.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracao do envio de e-mail transacional pelo Resend.
 *
 * <p>Sem {@code RESEND_API_KEY} definido o envio fica desligado e o link de
 * redefinicao e apenas escrito no log, o que mantem o fluxo utilizavel em
 * desenvolvimento sem depender de credenciais.
 *
 * @param apiKey        chave de API do Resend (re_...)
 * @param from          remetente no formato "Nome &lt;endereco@dominio&gt;"
 * @param replyTo       endereco opcional de resposta
 * @param appBaseUrl    origem usada para montar o link do e-mail
 * @param resetTokenTtl validade do link de redefinicao
 * @param maxRequestsPerHour teto de pedidos de redefinicao por usuario por hora
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(

        @DefaultValue("") String apiKey,
        @DefaultValue("Sentinela <onboarding@resend.dev>") String from,
        @DefaultValue("") String replyTo,
        @DefaultValue("http://localhost:5173") String appBaseUrl,
        @DefaultValue("30m") Duration resetTokenTtl,
        @DefaultValue("5") int maxRequestsPerHour
) {

    /** O envio real so acontece quando ha chave de API configurada. */
    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean hasReplyTo() {
        return replyTo != null && !replyTo.isBlank();
    }

    /** Base sem barra final, para concatenar com os caminhos do SPA. */
    public String normalizedBaseUrl() {
        String base = appBaseUrl == null || appBaseUrl.isBlank() ? "http://localhost:5173" : appBaseUrl.trim();
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
