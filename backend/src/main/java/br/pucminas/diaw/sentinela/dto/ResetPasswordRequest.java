package br.pucminas.diaw.sentinela.dto;

import br.pucminas.diaw.sentinela.validation.PasswordConfirmable;
import br.pucminas.diaw.sentinela.validation.PasswordConfirmation;
import br.pucminas.diaw.sentinela.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

/** Corpo do POST /api/auth/resetpassword: token do e-mail e a nova senha. */
@PasswordConfirmation
public record ResetPasswordRequest(

        @NotBlank(message = "Link de redefinicao invalido")
        String token,

        @NotBlank(message = "Informe uma senha")
        @StrongPassword
        String password,

        @NotBlank(message = "Confirme a senha")
        String confirmPassword

) implements PasswordConfirmable {
}
