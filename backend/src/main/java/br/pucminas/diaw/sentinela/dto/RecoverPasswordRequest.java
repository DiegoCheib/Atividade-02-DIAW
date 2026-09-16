package br.pucminas.diaw.sentinela.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecoverPasswordRequest(

        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "Informe um e-mail valido")
        String email
) {
}
