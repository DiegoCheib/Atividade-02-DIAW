package br.pucminas.diaw.sentinela.service.mail;

import java.time.Duration;

/**
 * Monta o e-mail de redefinicao de senha.
 *
 * <p>O HTML usa tabelas e estilo inline porque clientes de e-mail ignoram folhas
 * de estilo externas e boa parte do CSS moderno. Sempre acompanha uma versao em
 * texto puro com o mesmo link.
 */
public final class PasswordResetEmail {

    private static final String SUBJECT = "Redefinicao de senha - Sentinela PUC Minas";

    private PasswordResetEmail() {
    }

    public static EmailMessage build(String to, String name, String resetLink, Duration ttl) {
        String validade = validade(ttl);
        return new EmailMessage(to, SUBJECT, html(name, resetLink, validade), text(name, resetLink, validade));
    }

    private static String html(String name, String link, String validade) {
        return """
                <!doctype html>
                <html lang="pt-BR">
                  <body style="margin:0;padding:24px;background:#f1f5f9;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:#0f172a;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e2e8f0;">
                      <tr>
                        <td style="background:#005380;padding:28px 32px;color:#ffffff;">
                          <div style="font-size:20px;font-weight:600;letter-spacing:-0.01em;">Sentinela</div>
                          <div style="font-size:11px;letter-spacing:0.18em;text-transform:uppercase;opacity:0.75;margin-top:4px;">PUC Minas &middot; DIAW</div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px;">
                          <h1 style="margin:0 0 12px;font-size:22px;font-weight:600;">Redefinicao de senha</h1>
                          <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#334155;">
                            Ola, %s. Recebemos um pedido para redefinir a senha da sua conta.
                            Clique no botao abaixo para escolher uma nova senha.
                          </p>
                          <table role="presentation" cellpadding="0" cellspacing="0" style="margin:24px 0;">
                            <tr>
                              <td style="border-radius:12px;background:#005380;">
                                <a href="%s" style="display:inline-block;padding:14px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">
                                  Definir nova senha
                                </a>
                              </td>
                            </tr>
                          </table>
                          <p style="margin:0 0 16px;font-size:14px;line-height:1.6;color:#334155;">
                            O link vale por %s e pode ser usado uma unica vez.
                            Se voce nao pediu esta redefinicao, ignore esta mensagem: sua senha atual continua valendo.
                          </p>
                          <p style="margin:0;font-size:12px;line-height:1.6;color:#64748b;word-break:break-all;">
                            Se o botao nao funcionar, copie este endereco no navegador:<br>%s
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:18px 32px;background:#f8fafc;border-top:1px solid #e2e8f0;font-size:12px;color:#64748b;">
                          Mensagem automatica do Sentinela. Nao responda a este e-mail.
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(escape(name), link, validade, link);
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
