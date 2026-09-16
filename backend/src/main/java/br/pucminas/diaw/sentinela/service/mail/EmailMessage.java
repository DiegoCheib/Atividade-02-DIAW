package br.pucminas.diaw.sentinela.service.mail;

import java.util.List;

/**
 * Mensagem pronta para envio.
 *
 * @param to           destinatario
 * @param subject      assunto
 * @param html         corpo em HTML
 * @param text         alternativa em texto puro, para clientes que nao renderizam HTML
 * @param inlineImages imagens embutidas no corpo, referenciadas por {@code cid:}
 */
public record EmailMessage(String to, String subject, String html, String text,
                           List<InlineImage> inlineImages) {

    public EmailMessage {
        inlineImages = inlineImages == null ? List.of() : List.copyOf(inlineImages);
    }

    /** Mensagem sem imagens embutidas. */
    public EmailMessage(String to, String subject, String html, String text) {
        this(to, subject, html, text, List.of());
    }

    /**
     * Imagem embutida no corpo do e-mail. Diferente de uma URL, nao depende de o
     * servidor estar publicamente acessivel, entao funciona tambem em ambiente local.
     *
     * @param contentId identificador usado no HTML como {@code <img src="cid:ID">}
     * @param filename  nome do arquivo apresentado ao cliente de e-mail
     * @param base64    conteudo do arquivo em Base64
     */
    public record InlineImage(String contentId, String filename, String base64) {
    }
}
