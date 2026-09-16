package br.pucminas.diaw.sentinela.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72; // limite de bytes tratados pelo BCrypt

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank reporta o campo vazio
        }
        return isStrong(value);
    }

    /** Regra reutilizada tambem pelo fluxo de redefinicao de senha. */
    public static boolean isStrong(String value) {
        if (value == null || value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else if (!Character.isWhitespace(c)) {
                symbol = true;
            }
        }
        return upper && lower && digit && symbol;
    }
}
