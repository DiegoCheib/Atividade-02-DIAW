package br.pucminas.diaw.sentinela.dto;

import br.pucminas.diaw.sentinela.validation.PasswordConfirmable;
import br.pucminas.diaw.sentinela.validation.PasswordConfirmation;
import br.pucminas.diaw.sentinela.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordConfirmation
public record RegisterRequest(

        @NotBlank(message = "Informe seu nome")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        String name,

        @NotBlank(message = "Informe um nome de usuario")
        @Size(min = 3, max = 40, message = "O usuario deve ter entre 3 e 40 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Use apenas letras, numeros, ponto, hifen ou underline")
        String username,

        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "Informe um e-mail valido")
        @Size(max = 180, message = "E-mail muito longo")
        String email,

        @NotBlank(message = "Informe uma senha")
        @StrongPassword
        String password,

        @NotBlank(message = "Confirme a senha")
        String confirmPassword

) implements PasswordConfirmable {
}
