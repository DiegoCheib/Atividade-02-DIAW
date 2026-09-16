package br.pucminas.diaw.sentinela.dto;

import jakarta.validation.constraints.NotBlank;

/** O campo identifier aceita nome de usuario ou e-mail. */
public record LoginRequest(

        @NotBlank(message = "Informe o usuario ou e-mail")
        String identifier,

        @NotBlank(message = "Informe a senha")
        String password,

        boolean rememberMe
) {
}
