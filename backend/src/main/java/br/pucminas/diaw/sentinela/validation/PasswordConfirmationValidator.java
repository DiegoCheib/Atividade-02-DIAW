package br.pucminas.diaw.sentinela.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PasswordConfirmationValidator
        implements ConstraintValidator<PasswordConfirmation, PasswordConfirmable> {

    @Override
    public boolean isValid(PasswordConfirmable value, ConstraintValidatorContext context) {
        if (value == null || value.password() == null || value.confirmPassword() == null) {
            return true; // as anotacoes @NotBlank cuidam dos campos vazios
        }
        if (Objects.equals(value.password(), value.confirmPassword())) {
            return true;
        }
        // Reporta o erro no campo de confirmacao para o formulario destacar o input certo
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("confirmPassword")
                .addConstraintViolation();
        return false;
    }
}
