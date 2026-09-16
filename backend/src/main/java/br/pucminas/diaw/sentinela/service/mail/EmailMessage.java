package br.pucminas.diaw.sentinela.service.mail;

/**
 * Mensagem pronta para envio.
 *
 * @param to      destinatario
 * @param subject assunto
 * @param html    corpo em HTML
 * @param text    alternativa em texto puro, para clientes que nao renderizam HTML
 */
public record EmailMessage(String to, String subject, String html, String text) {
}
