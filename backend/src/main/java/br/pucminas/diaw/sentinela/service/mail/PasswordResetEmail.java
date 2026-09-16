package br.pucminas.diaw.sentinela.service.mail;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monta o e-mail de redefinicao de senha.
 *
 * <p>O HTML usa tabelas e estilo inline porque clientes de e-mail ignoram folhas
 * de estilo externas e boa parte do CSS moderno. Sempre acompanha uma versao em
 * texto puro com o mesmo link.
 *
 * <p>A logo da PUC Minas vai embutida como anexo inline referenciado por
 * {@code cid:}. Isso evita depender de uma URL publica, entao a imagem aparece
 * tambem quando a aplicacao roda apenas em {@code localhost}.
 */
public final class PasswordResetEmail {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmail.class);

    private static final String SUBJECT = "Redefinicao de senha - Sentinela PUC Minas";

    private static final String LOGO_RESOURCE = "/email/pucminas-logo.png";
    private static final String LOGO_CID = "pucminas-logo";
    private static final String LOGO_FILENAME = "pucminas-logo.png";

    /** Carregada uma unica vez: o arquivo acompanha o JAR e nunca muda em execucao. */
    private static final String LOGO_BASE64 = carregarLogo();

    private PasswordResetEmail() {
    }

    public static EmailMessage build(String to, String name, String resetLink, Duration ttl) {
        String validade = validade(ttl);
        List<EmailMessage.InlineImage> imagens = LOGO_BASE64 == null
                ? List.of()
                : List.of(new EmailMessage.InlineImage(LOGO_CID, LOGO_FILENAME, LOGO_BASE64));

        return new EmailMessage(to, SUBJECT,
                html(name, resetLink, validade), text(name, resetLink, validade), imagens);
    }

    private static String html(String name, String link, String validade) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                  <body style="margin:0;padding:24px;background:#f1f5f9;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" align="center" style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e2e8f0;">
                      <tr>
                        <td align="center" style="background:#005380;padding:28px 32px;text-align:center;">
                          <img src="cid:%s" alt="PUC Minas" width="96" height="96"
                               style="display:block;margin:0 auto;width:96px;height:96px;border:0;outline:none;text-decoration:none;">
                        </td>
                      </tr>
                      <tr>
                        <td align="center" style="padding:32px;text-align:center;">
                          <h1 style="margin:0 0 12px;font-size:22px;font-weight:600;text-align:center;">Redefinicao de senha</h1>
                          <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#334155;text-align:center;">
                            Ola, %s. Recebemos um pedido para redefinir a senha da sua conta.
                            Clique no botao abaixo para escolher uma nova senha.
                          </p>
                          <table role="presentation" cellpadding="0" cellspacing="0" align="center" style="margin:24px auto;">
                            <tr>
                              <td align="center" style="border-radius:12px;background:#005380;">
                                <a href="%s" style="display:inline-block;padding:14px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">
                                  Definir nova senha
                                </a>
                              </td>
                            </tr>
                          </table>
                          <p style="margin:0 0 16px;font-size:14px;line-height:1.6;color:#334155;text-align:center;">
                            O link vale por %s e pode ser usado uma unica vez.
                            Se voce nao pediu esta redefinicao, ignore esta mensagem: sua senha atual continua valendo.
                          </p>
                          <p style="margin:0;font-size:12px;line-height:1.6;color:#64748b;word-break:break-all;text-align:center;">
                            Se o botao nao funcionar, copie este endereco no navegador:<br>%s
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td align="center" style="padding:18px 32px;background:#f8fafc;border-top:1px solid #e2e8f0;font-size:12px;color:#64748b;text-align:center;">
                          Mensagem automatica do Sentinela. Nao responda a este e-mail.
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(LOGO_CID, escape(name), link, validade, link);
    }

    private static String text(String name, String link, String validade) {
        return """
                Ola, %s.

                Recebemos um pedido para redefinir a senha da sua conta no Sentinela.
                Abra o endereco abaixo para escolher uma nova senha:

                %s

                O link vale por %s e pode ser usado uma unica vez.
                Se voce nao pediu esta redefinicao, ignore esta mensagem: sua senha atual continua valendo.

                Sentinela - PUC Minas / DIAW
                """.formatted(name, link, validade);
    }

    /**
     * @return a logo em Base64, ou {@code null} se o recurso nao estiver no
     *         classpath. Nesse caso o e-mail sai sem a imagem, exibindo o texto
     *         alternativo: uma logo ausente nao justifica falhar a recuperacao.
     */
    private static String carregarLogo() {
        try (InputStream in = PasswordResetEmail.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (in == null) {
                log.warn("Logo {} nao encontrada no classpath: o e-mail sera enviado sem a imagem", LOGO_RESOURCE);
                return null;
            }
            return Base64.getEncoder().encodeToString(in.readAllBytes());
        } catch (IOException e) {
            log.warn("Falha ao ler a logo {}: {}", LOGO_RESOURCE, e.getMessage());
            return null;
        }
    }

    private static String validade(Duration ttl) {
        long minutos = Math.max(1, ttl.toMinutes());
        if (minutos < 60) {
            return minutos + " minutos";
        }
        long horas = ttl.toHours();
        return horas + (horas == 1 ? " hora" : " horas");
    }

    /** O nome vem do cadastro; escapar evita quebrar o HTML da mensagem. */
    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
