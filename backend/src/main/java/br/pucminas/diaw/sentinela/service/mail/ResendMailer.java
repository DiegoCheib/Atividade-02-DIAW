package br.pucminas.diaw.sentinela.service.mail;

import br.pucminas.diaw.sentinela.config.MailProperties;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Envio de e-mail pela API HTTP do Resend (POST https://api.resend.com/emails).
 *
 * <p>Usa o {@code RestClient} do proprio Spring, sem SDK adicional. Quando a
 * chave nao esta configurada o envio e ignorado e o conteudo vai para o log,
 * permitindo testar o fluxo completo em desenvolvimento.
 */
@Component
public class ResendMailer {

    private static final Logger log = LoggerFactory.getLogger(ResendMailer.class);
    private static final String API_BASE_URL = "https://api.resend.com";
    private static final String USER_AGENT = "sentinela-diaw/1.0";

    private final MailProperties properties;
    private final RestClient restClient;

    public ResendMailer(MailProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder
                .baseUrl(API_BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .requestFactory(timeouts())
                .build();
    }

    /**
     * @return {@code true} quando o Resend aceitou a mensagem. Falhas sao
     *         registradas e devolvidas como {@code false}: o chamador nunca
     *         expoe ao usuario se o e-mail existe ou se o provedor caiu.
     */
    public boolean send(EmailMessage message) {
        if (!properties.enabled()) {
            log.warn("RESEND_API_KEY nao configurada: e-mail para {} nao foi enviado. Assunto: {}",
                    message.to(), message.subject());
            log.info("Conteudo em texto do e-mail nao enviado:\n{}", message.text());
            return false;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", properties.from());
        payload.put("to", List.of(message.to()));
        payload.put("subject", message.subject());
        payload.put("html", message.html());
        payload.put("text", message.text());
        if (properties.hasReplyTo()) {
            payload.put("reply_to", properties.replyTo());
        }
        if (!message.inlineImages().isEmpty()) {
            payload.put("attachments", message.inlineImages().stream()
                    .map(ResendMailer::attachment)
                    .toList());
        }

        try {
            ResendResponse response = restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(ResendResponse.class);

            log.info("E-mail \"{}\" enviado pelo Resend (id {})", message.subject(),
                    response == null ? "desconhecido" : response.id());
            return true;
        } catch (RestClientException e) {
            log.error("Falha ao enviar e-mail pelo Resend: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Anexo inline no formato esperado pelo Resend. O {@code content_id} e o que
     * liga o arquivo ao {@code <img src="cid:...">} do corpo da mensagem.
     */
    private static Map<String, Object> attachment(EmailMessage.InlineImage image) {
        Map<String, Object> anexo = new LinkedHashMap<>();
        anexo.put("filename", image.filename());
        anexo.put("content", image.base64());
        anexo.put("content_id", image.contentId());
        return anexo;
    }

    /** Timeouts curtos: o pedido de recuperacao nao pode travar a requisicao HTTP. */
    private static SimpleClientHttpRequestFactory timeouts() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }

    /** Resposta do Resend: interessa apenas o id da mensagem, util para rastrear. */
    private record ResendResponse(String id) {
    }
}
