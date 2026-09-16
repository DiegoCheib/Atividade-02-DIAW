package br.pucminas.diaw.sentinela.dto;

/**
 * Confirmacao de que o link de redefinicao continua valido.
 *
 * @param email endereco mascarado (ex.: {@code di***@pucminas.br}), apenas para o
 *              usuario reconhecer a conta sem que a URL exponha o e-mail completo
 */
public record ResetTokenResponse(String email) {
}
